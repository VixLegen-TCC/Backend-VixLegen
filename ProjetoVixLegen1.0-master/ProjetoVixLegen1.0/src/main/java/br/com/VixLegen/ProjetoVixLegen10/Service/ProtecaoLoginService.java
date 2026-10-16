package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.TentativasLoginExcedidasException;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.util.Locale;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limite simples por conta e IP. Usa memória do processo; multi-instância deverá
 * usar Redis/Bucket4j para compartilhar contadores.
 */
@Service
public class ProtecaoLoginService {
    private static final long JANELA_MS = 15 * 60 * 1000L;
    private static final int LIMITE_CONTA = 5;
    private static final int LIMITE_IP = 30;
    private final Clock relogio;
    private final ConcurrentHashMap<String, Tentativas> contadores = new ConcurrentHashMap<>();
    private final AtomicInteger verificacoes = new AtomicInteger();

    private record Tentativas(int quantidade, long inicioMs) {}

    public ProtecaoLoginService(Clock relogio) {
        this.relogio = relogio;
    }

    public void verificar(String email, String ip) {
        long agora = relogio.millis();
        if (estaBloqueado("conta:" + normalizar(email), LIMITE_CONTA, agora)
                || estaBloqueado("ip:" + normalizarIp(ip), LIMITE_IP, agora)) {
            throw new TentativasLoginExcedidasException();
        }
        // Limpeza para não acumular registros antigos.
        if (verificacoes.incrementAndGet() % 256 == 0) {
            contadores.entrySet().removeIf(e -> agora - e.getValue().inicioMs() >= JANELA_MS);
        }
        if (contadores.size() > 20000) {
            contadores.entrySet().removeIf(e -> agora - e.getValue().inicioMs() >= JANELA_MS);
        }
    }

    public void falha(String email, String ip) {
        long agora = relogio.millis();
        somar("conta:" + normalizar(email), agora);
        somar("ip:" + normalizarIp(ip), agora);
    }

    public void sucesso(String email) {
        contadores.remove("conta:" + normalizar(email));
    }

    private boolean estaBloqueado(String chave, int limite, long agora) {
        Tentativas t = contadores.get(chave);
        return t != null && agora - t.inicioMs() < JANELA_MS && t.quantidade() >= limite;
    }

    private void somar(String chave, long agora) {
        contadores.compute(chave, (k, anterior) -> {
            if (anterior == null || agora - anterior.inicioMs() >= JANELA_MS) return new Tentativas(1, agora);
            return new Tentativas(anterior.quantidade() + 1, anterior.inicioMs());
        });
    }

    private String normalizar(String email) {
        // Não registrar e-mail nos logs do limitador.
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(email.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private String normalizarIp(String ip) {
        return ip == null || ip.isBlank() ? "desconhecido" : ip;
    }
}
