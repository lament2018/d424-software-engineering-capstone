package com.schedule.security;
import org.springframework.context.ApplicationContext;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanInspector {
    private final ApplicationContext context;

    public BeanInspector(ApplicationContext context) {
        this.context = context;
    }
    @PostConstruct
    public void logUserDetailsServiceBeans() {
        String[] beanNames = context.getBeanNamesForType(org.springframework.security.core.userdetails.UserDetailsService.class);
        System.out.println("🧠 Found " + beanNames.length + " UserDetailsService bean(s):");
        for (String name : beanNames) {
            Object bean = context.getBean(name);
            System.out.println("🔍 Bean name: " + name + " => " + bean.getClass().getName());
        }
    }
}
