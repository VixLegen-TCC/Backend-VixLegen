package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.TentativasLoginExcedidasException;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class ProtecaoLoginServiceTest {
    private final Instant agora = Instant.parse("2026-10-16T12:00:00Z");

    @Test
    void bloqueiaSextaTentativaDaConta() {
        ProtecaoLoginService svc = new ProtecaoLoginService(Clock.fixed(agora, ZoneOffset.UTC));
        for (int i = 0; i < 5; i++) {
            svc.verificar("teste@exemplo.com", "127.0.0.1");
            svc.falha("teste@exemplo.com", "127.0.0.1");
        }
        assertThrows(TentativasLoginExcedidasException.class,
                () -> svc.verificar("teste@exemplo.com", "127.0.0.1"));
    }

    @Test
    void bloqueioExpiraDepoisDaJanela() {
        ProtecaoLoginService svc = new ProtecaoLoginService(
                Clock.fixed(agora, ZoneOffset.UTC));
        for (int i = 0; i < 5; i++) svc.falha("teste@exemplo.com", "127.0.0.1");
        assertThrows(TentativasLoginExcedidasException.class,
                () -> svc.verificar("teste@exemplo.com", "127.0.0.1"));
        // Uma nova instância representa o reinício; o bloqueio em memória não é persistente.
        ProtecaoLoginService depois = new ProtecaoLoginService(
                Clock.offset(Clock.fixed(agora, ZoneOffset.UTC), Duration.ofMinutes(16)));
        assertDoesNotThrow(() -> depois.verificar("teste@exemplo.com", "127.0.0.1"));
    }

    @Test
    void sucessoLimpaTentativasDaConta() {
        ProtecaoLoginService svc = new ProtecaoLoginService(Clock.fixed(agora, ZoneOffset.UTC));
        for (int i = 0; i < 4; i++) svc.falha("teste@exemplo.com", "127.0.0.1");
        svc.sucesso("teste@exemplo.com");
        assertDoesNotThrow(() -> svc.verificar("teste@exemplo.com", "127.0.0.1"));
    }
}
