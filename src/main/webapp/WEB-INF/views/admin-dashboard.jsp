<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%@ taglib prefix="ag" uri="agrogest-formatos" %><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<t:layout titulo="Visão geral">
  <div class="page-title">
    <div><span class="eyebrow">O SEU NEGÓCIO, EM PERSPECTIVA</span><h1>Visão geral</h1><p>Bem-vindo, <c:out value="${utilizador.nome}"/>. Acompanhe o que faz crescer.</p></div>
    <a class="btn secondary small" href="${cp}/catalogo"><t:icone nome="folha"/>Ver catálogo <span aria-hidden="true">↗</span></a>
  </div>
  <section class="hero dashboard-hero" aria-labelledby="dashboard-intro">
    <div class="hero-content"><span class="eyebrow">DA TERRA AO FUTURO</span><h2 id="dashboard-intro">Cultive uma visão <em>mais clara.</em></h2><p>Campanhas, colheitas e comércio. Cada parte do seu negócio, no mesmo lugar.</p><a class="hero-link" href="${cp}/admin/campanhas">Acompanhar campanhas <span aria-hidden="true">→</span></a></div>
    <img src="${cp}/assets/img/terraces.svg" alt="" width="560" height="300">
  </section>
  <div class="section-heading"><h2>O essencial do seu negócio</h2><span>Todos os registos</span></div>
  <div class="stats">
    <div class="stat money"><span>Volume de vendas</span><div class="stat-icon"><t:icone nome="grafico"/></div><strong>${ag:moeda(totalVendas)} <small>MT</small></strong><small>${numVendas} compras · IVA incluído</small></div>
    <div class="stat"><span>Produtos</span><div class="stat-icon"><t:icone nome="caixa"/></div><strong>${numProdutos}</strong><small>produtos no catálogo</small></div>
    <div class="stat"><span>Clientes</span><div class="stat-icon"><t:icone nome="pessoa"/></div><strong>${numClientes}</strong><small>contas de cliente</small></div>
    <div class="stat"><span>Campanhas</span><div class="stat-icon"><t:icone nome="calendario"/></div><strong>${numCampanhas}</strong><small>${numFornecedores} fornecedores parceiros</small></div>
  </div>
  <div class="two-columns">
    <div>
      <section class="panel"><div class="panel-header"><div><h2>Últimas compras</h2><p>As movimentações mais recentes.</p></div><a href="${cp}/admin/compras">Ver todas →</a></div>
        <div class="table-wrap"><table><thead><tr><th scope="col">Factura / data</th><th scope="col">Cliente</th><th scope="col" class="num">Total</th></tr></thead><tbody>
          <c:forEach items="${recentes}" var="v"><tr><td><a href="${cp}/factura?id=${v.id}"><strong><c:out value="${v.numero}"/></strong></a><small>${ag:data(v.data)}</small></td><td><c:out value="${v.clienteNome}"/></td><td class="num"><strong>${ag:moeda(v.total)} MT</strong></td></tr></c:forEach>
          <c:if test="${empty recentes}"><tr><td colspan="3" class="empty">Ainda não há compras.</td></tr></c:if>
        </tbody></table></div>
      </section>
      <section class="panel"><div class="panel-header"><div><h2>As suas campanhas</h2><p>Cada ciclo de produção, acompanhado.</p></div><a href="${cp}/admin/campanhas">Gerir →</a></div>
        <div class="summary-list"><c:forEach items="${campanhas}" var="c"><div class="summary-row"><div><strong><c:out value="${c.nome}"/></strong><br><small class="muted">${ag:data(c.inicio)} → ${ag:data(c.fim)}</small></div><span class="badge ${c.fechada ? 'gray' : ''}">${c.fechada ? 'Fechada' : 'Aberta'}</span></div></c:forEach>
          <c:if test="${empty campanhas}"><div class="empty"><p>Crie a sua primeira campanha.</p><a class="btn small" href="${cp}/admin/campanhas">Criar campanha</a></div></c:if>
        </div>
      </section>
    </div>
    <div class="stack dashboard-side">
      <section class="panel" aria-labelledby="sales-chart-title"><div class="panel-header"><div><h2 id="sales-chart-title">Valor por compra</h2><p>Últimas ${fn:length(recentes)} compras · IVA incluído</p></div><t:icone nome="grafico"/></div>
        <c:set var="maiorVenda" value="${0}"/><c:forEach items="${recentes}" var="v"><c:if test="${v.total > maiorVenda}"><c:set var="maiorVenda" value="${v.total}"/></c:if></c:forEach>
        <div class="sales-chart"><c:forEach items="${recentes}" var="v"><div class="chart-row"><div class="chart-label"><a href="${cp}/factura?id=${v.id}"><c:out value="${v.numero}"/></a><strong>${ag:moeda(v.total)} MT</strong></div><div class="chart-track" aria-hidden="true"><span style="width:${maiorVenda > 0 ? v.total * 100 / maiorVenda : 0}%"></span></div></div></c:forEach></div>
        <c:choose><c:when test="${empty recentes}"><p class="chart-caption">Os valores serão apresentados após a primeira compra.</p></c:when><c:otherwise><p class="chart-caption">Valores em meticais · da compra mais recente à mais antiga.</p></c:otherwise></c:choose>
      </section>
      <section class="panel"><div class="panel-header"><h2>Do plantio à colheita</h2><t:icone nome="campo"/></div><div class="summary-list"><a class="summary-row" href="${cp}/admin/cultivos"><strong>Consultar cultivos</strong><span aria-hidden="true">↗</span></a><a class="summary-row" href="${cp}/admin/colheitas"><strong>Registar uma colheita</strong><span aria-hidden="true">↗</span></a><a class="summary-row" href="${cp}/admin/armazem"><strong>Consultar armazém</strong><span aria-hidden="true">↗</span></a></div></section>
    </div>
  </div>
  <div class="section-heading"><h2>A sua próxima acção</h2></div>
  <div class="quick-links dashboard-actions"><a class="quick-card" href="${cp}/admin/produtos"><t:icone nome="caixa"/><strong>Gerir o catálogo</strong><p>Produtos, preços e reposição de stock.</p></a><a class="quick-card" href="${cp}/admin/fornecedores"><t:icone nome="fornecedor"/><strong>Gerir parceiros</strong><p>Os fornecedores do seu negócio.</p></a><a class="quick-card" href="${cp}/admin/relatorios"><t:icone nome="grafico"/><strong>Conhecer os clientes</strong><p>Volume de compras e assiduidade.</p></a></div>
</t:layout>
