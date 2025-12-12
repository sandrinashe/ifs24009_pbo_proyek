package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Gunakan MockitoExtension, BUKAN SpringExtension/WebMvcTest
// Ini jauh lebih ringan dan tidak error dependency.
@ExtendWith(MockitoExtension.class)
class WebMvcConfigTest {

    // 1. Mock InterceptorRegistry (Papan pendaftaran)
    @Mock
    private InterceptorRegistry registry;

    // 2. Mock InterceptorRegistration (Hasil pendaftaran)
    @Mock
    private InterceptorRegistration registration;

    // 3. Mock AuthInterceptor (Object interceptor kamu)
    @Mock
    private AuthInterceptor authInterceptor;

    // 4. Inject Mock ke dalam WebMvcConfig
    @InjectMocks
    private WebMvcConfig webMvcConfig;

    @Test
    void addInterceptors_ShouldRegisterAuthInterceptorWithCorrectPaths() {
        // --- ARRANGE (Persiapan) ---
        
        // Ketika registry.addInterceptor dipanggil, kembalikan objek 'registration' palsu
        // Ini perlu karena method-nya chaining (berantai: .add().exclude().exclude())
        when(registry.addInterceptor(authInterceptor)).thenReturn(registration);
        
        // Agar tidak NullPointerException saat chaining method, kita return diri sendiri
        when(registration.addPathPatterns(anyString())).thenReturn(registration);
        when(registration.excludePathPatterns(anyString())).thenReturn(registration);

        // --- ACT (Eksekusi) ---
        
        // Panggil method aslinya
        webMvcConfig.addInterceptors(registry);

        // --- ASSERT (Verifikasi) ---

        // 1. Pastikan interceptor yang didaftarkan adalah authInterceptor
        verify(registry).addInterceptor(authInterceptor);

        // 2. Pastikan pattern "/api/**" ditambahkan
        verify(registration).addPathPatterns("/api/**");

        // 3. Pastikan pattern exclude "/api/auth/**" ditambahkan
        verify(registration).excludePathPatterns("/api/auth/**");

        // 4. Pastikan pattern exclude "/api/public/**" ditambahkan
        verify(registration).excludePathPatterns("/api/public/**");
    }
}