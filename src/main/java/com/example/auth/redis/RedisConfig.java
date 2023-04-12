package com.example.auth.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.KotlinDetector;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.util.ClassUtils;

/**
 * Created by berg on 2023/4/8.
 */
@Configuration
public class RedisConfig {

    @Bean("springSessionDefaultRedisSerializer")
    public RedisSerializer<Object> defaultRedisSerializer() {
        // 参考 RedisSerializer.json();
        ObjectMapper mapper = new ObjectMapper();
        StdTypeResolverBuilder typer = new TypeResolverBuilder(ObjectMapper.DefaultTyping.EVERYTHING,
                mapper.getPolymorphicTypeValidator());
        typer = typer.init(JsonTypeInfo.Id.CLASS, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        mapper.setDefaultTyping(typer);
        GenericJackson2JsonRedisSerializer.registerNullValueSerializer(mapper, null);
        mapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    /**
     * Custom {@link StdTypeResolverBuilder} that considers typing for non-primitive types. Primitives, their wrappers and
     * primitive arrays do not require type hints. The default {@code DefaultTyping#EVERYTHING} typing does not satisfy
     * those requirements.
     *
     * @author Mark Paluch
     * @since 2.7.2
     */
    private static class TypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {

        public TypeResolverBuilder(ObjectMapper.DefaultTyping t, PolymorphicTypeValidator ptv) {
            super(t, ptv);
        }

        @Override
        public ObjectMapper.DefaultTypeResolverBuilder withDefaultImpl(Class<?> defaultImpl) {
            return this;
        }

        /**
         * Method called to check if the default type handler should be used for given type. Note: "natural types" (String,
         * Boolean, Integer, Double) will never use typing; that is both due to them being concrete and final, and since
         * actual serializers and deserializers will also ignore any attempts to enforce typing.
         */
        public boolean useForType(JavaType t) {

            if (t.isJavaLangObject()) {
                return true;
            }

            t = resolveArrayOrWrapper(t);

            if (t.isEnumType() || ClassUtils.isPrimitiveOrWrapper(t.getRawClass())) {
                return false;
            }

            if (t.isFinal() && !KotlinDetector.isKotlinType(t.getRawClass())
                    && t.getRawClass().getPackage().getName().startsWith("java")) {
                return false;
            }

            // [databind#88] Should not apply to JSON tree models:
            return !TreeNode.class.isAssignableFrom(t.getRawClass());
        }

        private JavaType resolveArrayOrWrapper(JavaType type) {

            while (type.isArrayType()) {
                type = type.getContentType();
                if (type.isReferenceType()) {
                    type = resolveArrayOrWrapper(type);
                }
            }

            while (type.isReferenceType()) {
                type = type.getReferencedType();
                if (type.isArrayType()) {
                    type = resolveArrayOrWrapper(type);
                }
            }

            return type;
        }
    }

}
