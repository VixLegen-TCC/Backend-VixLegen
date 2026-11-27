package br.com.VixLegen.ProjetoVixLegen10.Service;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
public class RagSessaoRegistroSmoke {
    static int ok;
    static void check(boolean value) { if (!value) throw new AssertionError(); ok++; }
    static void falha(Runnable r, RagSessaoRegistro.Falha f) {
        try { r.run(); throw new AssertionError("Deveria falhar"); }
        catch (RagSessaoRegistro.Erro ex) { check(ex.falha == f); }
    }
    public static void main(String[] args) {
        var clock = new AtomicLong();
        var reg = new RagSessaoRegistro(clock::get, 100, 2);
        var a = new RagSessaoRegistro.Dono(1L, 10L);
        var b = new RagSessaoRegistro.Dono(2L, 10L);
        var c = new RagSessaoRegistro.Dono(1L, 11L);
        var interna = UUID.randomUUID(); var id = reg.registrar(a, interna);
        check(!id.equals(interna));
        falha(() -> reg.reservar(id,b), RagSessaoRegistro.Falha.AUSENTE);
        falha(() -> reg.reservar(id,c), RagSessaoRegistro.Falha.AUSENTE);
        check(reg.reservar(id,a).equals(interna));
        falha(() -> reg.reservar(id,a), RagSessaoRegistro.Falha.OCUPADA);
        clock.set(120);
        falha(() -> reg.reservar(id,a), RagSessaoRegistro.Falha.OCUPADA);
        var outra = reg.registrar(b,UUID.randomUUID());
        falha(reg::conferirCapacidade, RagSessaoRegistro.Falha.LIMITE);
        reg.liberar(id,false);
        check(reg.reservar(id,a).equals(interna));
        reg.liberar(id,true);
        falha(() -> reg.reservar(id,a), RagSessaoRegistro.Falha.AUSENTE);
        clock.set(220);
        falha(() -> reg.reservar(outra,b), RagSessaoRegistro.Falha.AUSENTE);
        reg.conferirCapacidade();
        System.out.println(ok + " verificações aprovadas (registro em memória)");
    }
}
