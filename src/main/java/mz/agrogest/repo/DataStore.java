package mz.agrogest.repo;

/** Um único repositório por aplicação. Todos os serviços usam o MESMO monitor. */
public class DataStore {
    private final Estado estado;
    public DataStore() { this(new Estado()); }
    public DataStore(Estado estado) { this.estado = estado; }
    /** Usar apenas dentro de synchronized(store). */
    public Estado estado() { return estado; }
}
