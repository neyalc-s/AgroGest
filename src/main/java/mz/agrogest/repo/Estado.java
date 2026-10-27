package mz.agrogest.repo;

import mz.agrogest.model.*;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** É este conjunto de listas/mapas que é gravado no ficheiro .ser. */
public class Estado implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Map<Integer, Utilizador> utilizadores = new LinkedHashMap<>();
    public final Map<Integer, Fornecedor> fornecedores = new LinkedHashMap<>();
    public final Map<Integer, Produto> produtos = new LinkedHashMap<>();
    public final Map<Integer, Campanha> campanhas = new LinkedHashMap<>();
    public final Map<Integer, Cultivo> cultivos = new LinkedHashMap<>();
    public final Map<Integer, Colheita> colheitas = new LinkedHashMap<>();
    public final Map<Integer, Venda> vendas = new LinkedHashMap<>();
    // Um contador por tipo. São serializados para não repetir IDs após reiniciar.
    public final AtomicInteger utilizadorId = new AtomicInteger();
    public final AtomicInteger fornecedorId = new AtomicInteger();
    public final AtomicInteger produtoId = new AtomicInteger();
    public final AtomicInteger campanhaId = new AtomicInteger();
    public final AtomicInteger cultivoId = new AtomicInteger();
    public final AtomicInteger colheitaId = new AtomicInteger();
    public final AtomicInteger vendaId = new AtomicInteger();
}
