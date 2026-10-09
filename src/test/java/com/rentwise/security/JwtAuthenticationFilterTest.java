package com.rentwise.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {
    @Test
    void downstreamRuntimeExceptionIsNotReclassifiedAsInvalidJwt() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        RestAuthenticationEntryPoint entryPoint = mock(RestAuthenticationEntryPoint.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, entryPoint);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtService.parse("valid-token"))
                .thenReturn(new CurrentUser(1L, "learner", com.rentwise.user.domain.UserRole.LEARNER));
        FilterChain downstream = (req, res) -> {
            throw new IllegalStateException("downstream failure");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, downstream))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("downstream failure");
        verify(entryPoint, never()).commence(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }
}
