package app.testero.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Localised message bundles for user-facing backend text (notifications, and later errors).
 *
 * <p>Explicit rather than relying on the auto-configured bean so the two settings that matter
 * for correctness are pinned: UTF-8 (Italian has accented characters) and no fallback to the
 * server's system locale — an unset/unknown locale must resolve to the Italian default bundle,
 * never to whatever the host machine happens to be.
 */
@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        return source;
    }
}
