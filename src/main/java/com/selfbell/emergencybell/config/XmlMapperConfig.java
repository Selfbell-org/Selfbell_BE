package com.selfbell.emergencybell.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.stream.XMLInputFactory;

@Configuration
public class XmlMapperConfig {

    @Bean
    public XmlMapper xmlMapper() {
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);

        XMLInputFactory xif = XMLInputFactory.newFactory();
        // 긴 텍스트 노드 합치기
        xif.setProperty(XMLInputFactory.IS_COALESCING, true);
        // DTD 비활성화(보안/호환)
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        XmlMapper mapper = new XmlMapper(xif);
        mapper.registerModule(module);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return mapper;
    }
}
