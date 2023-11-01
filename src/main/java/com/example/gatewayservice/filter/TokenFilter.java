package com.example.gatewayservice.filter;

import com.example.gatewayservice.util.JwtUtil;
import com.example.gatewayservice.dto.CustomDto;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
public class TokenFilter extends AbstractGatewayFilterFactory<CustomDto> {

    private final JwtUtil jwtUtil;

    public TokenFilter(JwtUtil jwtUtil) {
        super(CustomDto.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(CustomDto config) {
        return (exchange, chain) -> {

            String token = "default";

            try {
                // 쿠키에서 토큰을 추출
                token = exchange.getRequest()
                        .getCookies()
                        .getFirst("AccessToken")
                        .getValue();

            } catch (NullPointerException e) {
                System.out.println("쿠키없음");
                return sendErrorResponse(exchange, "Unauthorized: Token Not Found");
            }

            String validateTokenResult = jwtUtil.validateToken(token);
            
            // 유효성 검사 에러 발생
            if (validateTokenResult != "Authorized") {
                return sendErrorResponse(exchange, validateTokenResult);
            }

            // 토큰을 파싱하여 사용자 ID를 추출 (UUID)
            UUID memberId = jwtUtil.getMemberIdFromToken(token);

            String role = jwtUtil.getRoleFromToken(token);

            // 사용자 ID를 요청 헤더에 추가
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("member-id", String.valueOf(memberId))
                    .header("role",role)
                    .build();
            System.out.println(memberId);

            // 업데이트된 요청으로 계속 진행
            return chain.filter(exchange.mutate().request(request).build());

        };
    }

    private Mono<Void> sendErrorResponse(ServerWebExchange exchange, String errorMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

}