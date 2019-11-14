package nextstep.di.factory;

import nextstep.annotation.Bean;
import nextstep.annotation.Configuration;
import nextstep.di.factory.exception.CannotCreateInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationBeanScanner {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationBeanScanner.class);

    private BeanFactory beanFactory;

    public ConfigurationBeanScanner(final BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void register(final Class<?> clazz) {
        if (clazz.isAnnotationPresent(Configuration.class)) {
            beanFactory.instantiate(clazz);
            registerBeanMethods(clazz, clazz.getDeclaredMethods());
        }
    }

    private void registerBeanMethods(final Class<?> clazz, final Method[] methods) {
        for (Method method : methods) {
            registerBeanMethod(clazz, method);
        }
    }

    private void registerBeanMethod(final Class<?> clazz, final Method method) {
        if (method.isAnnotationPresent(Bean.class)) {
            registerBean(clazz, method);
        }
    }

    private Object registerBean(final Class<?> clazz, final Method method) {
        try {
            if (beanFactory.hasBean(method.getReturnType())) {
                return beanFactory.getBean(method.getReturnType());
            }

            if (method.getParameterCount() == 0) {
                Object object = method.invoke(beanFactory.getBean(clazz));
                beanFactory.addBean(method.getReturnType(), object);

                return object;
            }

//            beanFactory.addBean(method.getReturnType(), method.invoke(clazz, aaa(clazz, method)));
            beanFactory.addBean(method.getReturnType(), method.invoke(beanFactory.getBean(clazz), aaa(clazz, method)));
            return null;

        } catch (Exception e) {
            logger.error(">> registerBean", e);
            throw new CannotCreateInstance(e);
        }
    }

    private Object[] aaa(final Class<?> clazz, final Method method) {
        List<Object> classes = new ArrayList<>();

        for (Class<?> parameterType : method.getParameterTypes()) {
            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                if (declaredMethod.getReturnType().equals(parameterType)) {
                    classes.add(registerBean(clazz, declaredMethod));
                }
            }
        }

        return classes.toArray();
    }
}
