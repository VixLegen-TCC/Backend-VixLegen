package br.com.VixLegen.ProjetoVixLegen10.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

/** Registro local: o UUID externo nunca é usado como autorização. */
final class RagSessaoRegistro {
    record Dono(Long usuario, Long empresa) {}
    enum Falha { AUSENTE, OCUPADA, LIMITE }
    static final class Erro extends RuntimeException {
        final Falha falha;
        Erro(Falha falha) { this.falha = falha; }
    }
    private static final class Sessao {
        final Dono dono; final UUID interna;
        long tocada; boolean ocupada;
        Sessao(Dono dono, UUID interna, long tocada) {
            this.dono = dono; this.interna = interna; this.tocada = tocada;
        }
    }
    private final Map<UUID, Sessao> sessoes = new HashMap<>();
    private final LongSupplier relogio;
    private final long ttl;
    private final int limite;
    RagSessaoRegistro(LongSupplier relogio, long ttl, int limite) {
        this.relogio = relogio; this.ttl = ttl; this.limite = limite;
    }
    private void limpar() {
        long agora = relogio.getAsLong();
        sessoes.values().removeIf(s -> !s.ocupada && agora - s.tocada >= ttl);
    }
    synchronized void conferirCapacidade() {
        limpar();
        if (sessoes.size() >= limite) throw new Erro(Falha.LIMITE);
    }
    synchronized UUID registrar(Dono dono, UUID interna) {
        conferirCapacidade();
        UUID externa = UUID.randomUUID();
        sessoes.put(externa, new Sessao(dono, interna, relogio.getAsLong()));
        return externa;
    }
    synchronized UUID reservar(UUID externa, Dono dono) {
        limpar();
        Sessao s = sessoes.get(externa);
        // Mesmo erro para inexistente e para pertencente a outra conta/empresa.
        if (s == null || !s.dono.equals(dono)) throw new Erro(Falha.AUSENTE);
        if (s.ocupada) throw new Erro(Falha.OCUPADA);
        s.ocupada = true;
        return s.interna;
    }
    synchronized void liberar(UUID externa, boolean remover) {
        if (remover) { sessoes.remove(externa); return; }
        Sessao s = sessoes.get(externa);
        if (s != null) { s.ocupada = false; s.tocada = relogio.getAsLong(); }
    }
}
