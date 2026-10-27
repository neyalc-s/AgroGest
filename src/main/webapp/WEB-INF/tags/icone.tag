<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="nome" required="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Desenhos no próprio HTML: nenhum pedido externo para apresentar os ícones. --%>
<svg class="icon" viewBox="0 0 24 24" width="22" height="22" aria-hidden="true" focusable="false"><c:choose>
<c:when test="${nome == 'folha'}"><path d="M20 3C9 2 3 7 5 14s13 5 15-11Z"/><path d="M3 21 15 9M8 16l-1-6m2 5 6 1"/></c:when>
<c:when test="${nome == 'painel'}"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></c:when>
<c:when test="${nome == 'fornecedor'}"><path d="M3 6h12v11H3zM15 10h3l3 4v3h-6"/><circle cx="7" cy="18" r="2"/><circle cx="18" cy="18" r="2"/></c:when>
<c:when test="${nome == 'caixa'}"><path d="m3 7 9-4 9 4v11l-9 4-9-4ZM3 7l9 4 9-4M12 11v11M7 5l9 4"/></c:when>
<c:when test="${nome == 'calendario'}"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4m10-4v4M3 10h18M7 14h3m4 0h3m-10 4h3"/></c:when>
<c:when test="${nome == 'campo'}"><path d="M2 13c5-5 15-5 20 0M2 18c6-5 15-4 20 1M6 22c3-3 6-4 10-4M12 11V5m0 3c-5 0-5-5-5-5 5 0 5 5 5 5Zm0 0c5 0 5-5 5-5-5 0-5 5-5 5Z"/></c:when>
<c:when test="${nome == 'recibo'}"><path d="M5 3h14v19l-3-2-4 2-4-2-3 2ZM8 7h8m-8 4h8m-8 4h4"/></c:when>
<c:when test="${nome == 'lista'}"><path d="M8 6h13M8 12h13M8 18h13M3 6h1m-1 6h1m-1 6h1"/></c:when>
<c:when test="${nome == 'grafico'}"><path d="M3 3v18h18M7 17v-5m5 5V8m5 9V4"/></c:when>
<c:when test="${nome == 'guardar'}"><path d="M4 3h13l4 4v14H3V3ZM7 3v6h10V3M7 21v-7h10v7"/></c:when>
<c:when test="${nome == 'pessoa'}"><circle cx="12" cy="8" r="4"/><path d="M4 22v-2a8 8 0 0 1 16 0v2"/></c:when>
<c:when test="${nome == 'carrinho'}"><path d="M2 3h3l3 13h12l2-9H6"/><circle cx="10" cy="21" r="1"/><circle cx="19" cy="21" r="1"/></c:when>
<c:when test="${nome == 'sair'}"><path d="M9 4H3v16h6m6-12 4 4-4 4m-8-4h12"/></c:when>
<c:when test="${nome == 'certo'}"><path d="m5 12 4 4L20 5"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h9"/></c:when>
</c:choose></svg>
