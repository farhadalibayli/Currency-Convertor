package com.example.servicea.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "ValCurs")
public class CbarResponse {
    
    @JacksonXmlProperty(localName = "Date")
    private String date;
    
    @JacksonXmlProperty(localName = "Name")
    private String name;
    
    @JacksonXmlProperty(localName = "Description")
    private String description;
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "ValType")
    private List<ValType> valTypes;
    
    // Constructors
    public CbarResponse() {}
    
    public CbarResponse(String date, String name, String description, List<ValType> valTypes) {
        this.date = date;
        this.name = name;
        this.description = description;
        this.valTypes = valTypes;
    }
    
    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<ValType> getValTypes() { return valTypes; }
    public void setValTypes(List<ValType> valTypes) { this.valTypes = valTypes; }
    
    public static class ValType {
        
        @JacksonXmlProperty(localName = "Type")
        private String type;
        
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "Valute")
        private List<Valute> valutes;
        
        // Constructors
        public ValType() {}
        
        public ValType(String type, List<Valute> valutes) {
            this.type = type;
            this.valutes = valutes;
        }
        
        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public List<Valute> getValutes() { return valutes; }
        public void setValutes(List<Valute> valutes) { this.valutes = valutes; }
    }
    
    public static class Valute {
        
        @JacksonXmlProperty(localName = "Code")
        private String code;
        
        @JacksonXmlProperty(localName = "Nominal")
        private String nominal;
        
        @JacksonXmlProperty(localName = "Name")
        private String name;
        
        @JacksonXmlProperty(localName = "Value")
        private String value;
        
        // Constructors
        public Valute() {}
        
        public Valute(String code, String nominal, String name, String value) {
            this.code = code;
            this.nominal = nominal;
            this.name = name;
            this.value = value;
        }
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getNominal() { return nominal; }
        public void setNominal(String nominal) { this.nominal = nominal; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
