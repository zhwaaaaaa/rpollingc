package com.zhw.rpollingc.request.protocol;

import com.zhw.rpollingc.request.remote.ErrorResponseException;
import com.zhw.rpollingc.request.remote.Exception;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ResultDecoder {

    private ObjectMapper objectMapper;

    public ResultDecoder() {
        objectMapper = initObjectMapper(true);
    }

    public ResultDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public <T> T decode(String cs, TypeReference<T> type) throws Exception {
        if (StringUtil.isNullOrEmpty(cs)) {
            return null;
        }
        if (type.getType() == String.class) {
            return (T) cs;
        }
        try {
            return objectMapper.readValue(cs, type);
        } catch (IOException e) {
            throw new ErrorResponseException("decode result from server for " + type.getType(), e);
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static ObjectMapper initObjectMapper(boolean nullable) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (!nullable) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        }
        objectMapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        class DateJsonSerializer extends JsonSerializer<Date> {
            @Override
            public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(new SimpleDateFormat("yyyyMMdd").format(value));
            }
        }

        class DateJsonDeserializer extends JsonDeserializer<Date> {

            @Override
            public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                String valueAsString = p.getValueAsString();
                try {
                    String s = valueAsString.replaceAll("-", "");
                    return new SimpleDateFormat("yyyyMMdd").parse(s);
                } catch (ParseException e) {
                    throw new JsonParseException(p, valueAsString);
                }
            }
        }
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(Date.class, new DateJsonSerializer());
        javaTimeModule.addDeserializer(Date.class, new DateJsonDeserializer());
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        objectMapper.registerModule(javaTimeModule);
        SimpleModule doubleSerializer = new SimpleModule().addDeserializer(Double.class, new JsonDeserializer<Double>() {
            @Override
            public Double deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                NumberDeserializers.DoubleDeserializer doubleDeserializer = new NumberDeserializers.DoubleDeserializer(Double.class, null);
                Double aDouble = doubleDeserializer.deserialize(jsonParser, deserializationContext);
                return aDouble.isNaN() ? null : aDouble;
            }
        });
        objectMapper.registerModule(doubleSerializer);
        objectMapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);

        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig();
        // 数据没有统一使用下划线或者驼峰。所以这里优先已驼峰方式去匹配。匹配不上则下划线转为驼峰取匹配
        objectMapper.setConfig(deserializationConfig.withHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt,
                                                 JsonParser p,
                                                 JsonDeserializer<?> deserializer,
                                                 Object beanOrClass,
                                                 String propertyName) throws IOException {

                if (deserializer instanceof BeanDeserializer) {
                    String s = tryTranslateToCamelCase(propertyName);
                    // 如果长度没有发生变化，则证明没有下划线
                    if (s.length() != propertyName.length()) {
                        BeanDeserializer base = (BeanDeserializer) deserializer;
                        SettableBeanProperty property = base.findProperty(s);
                        if (property != null) {
                            property.deserializeAndSet(p, ctxt, beanOrClass);
                            return true;
                        }
                    }
                }
                return false;
            }
        }));

        return objectMapper;
    }

    private static String tryTranslateToCamelCase(String name) {

        char[] cs = name.toCharArray();
        int len = cs.length;
        // 下划线转驼峰
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = cs[i];
            if (c == '_') {
                char cn;
                if (++i < len && (cn = cs[i]) != '_') {
                    sb.append(Character.toUpperCase(cn));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
