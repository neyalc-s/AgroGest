<%@ tag pageEncoding="UTF-8" %><%@ attribute name="titulo" required="true" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!doctype html><html lang="pt-MZ"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="theme-color" content="#183D32"><title><c:out value="${titulo}"/> · AgroGest</title><link rel="icon" href="${cp}/assets/favicon.svg" type="image/svg+xml"><link rel="stylesheet" href="${cp}/assets/style.css?v=botanical-2"><script defer src="${cp}/assets/app.js?v=botanical-1"></script><noscript><style>@media(max-width:960px){.shell{display:block}.sidebar{position:static;transform:none;visibility:visible;width:100%;padding:24px}.sidebar nav{display:flex;flex-wrap:wrap;gap:6px}.sidebar nav>a{flex:1 1 160px}.sidebar-label,.sidebar-bottom,.nav-section,.sidebar-close,.menu-toggle{display:none}.workspace{width:100%;margin:0}}</style></noscript></head>
<body><a href="#conteudo" class="skip">Saltar para o conteúdo</a><div id="estado-navegacao" class="sr-only" role="status" aria-live="polite"></div><div class="shell" data-utilizador="${utilizador.id}" data-versao="botanical-1">
<button class="nav-overlay" type="button" aria-label="Fechar menu" tabindex="-1" aria-hidden="true"></button>
<aside class="sidebar" id="navegacao" aria-label="Navegação principal"><button class="sidebar-close" type="button" aria-label="Fechar menu">×</button><a class="brand" href="${cp}/inicio"><t:marca/></a><div class="sidebar-label">${utilizador.perfil == 'ADMIN' ? 'ESPAÇO DO ADMINISTRADOR' : 'ESPAÇO DO CLIENTE'}</div>
<nav aria-label="Menu principal"><c:choose><c:when test="${utilizador.perfil == 'ADMIN'}">
<a class="${menu == 'dashboard' ? 'active' : ''}" href="${cp}/admin"><t:icone nome="painel"/>Visão geral</a>
<div class="nav-section">GESTÃO AGRÍCOLA</div>
<a class="${menu == 'fornecedores' ? 'active' : ''}" href="${cp}/admin/fornecedores"><t:icone nome="fornecedor"/>Fornecedores</a>
<a class="${menu == 'produtos' ? 'active' : ''}" href="${cp}/admin/produtos"><t:icone nome="caixa"/>Produtos</a>
<a class="${menu == 'campanhas' ? 'active' : ''}" href="${cp}/admin/campanhas"><t:icone nome="calendario"/>Campanhas</a>
<a class="${menu == 'cultivos' ? 'active' : ''}" href="${cp}/admin/cultivos"><t:icone nome="folha"/>Cultivos</a>
<a class="${menu == 'colheitas' ? 'active' : ''}" href="${cp}/admin/colheitas"><t:icone nome="campo"/>Colheitas</a>
<div class="nav-section">COMÉRCIO E ANÁLISE</div>
<a class="${menu == 'clientes' ? 'active' : ''}" href="${cp}/admin/clientes"><t:icone nome="pessoa"/>Clientes</a>
<a class="${menu == 'armazem' ? 'active' : ''}" href="${cp}/admin/armazem"><t:icone nome="caixa"/>Armazém</a>
<a class="${menu == 'compras' ? 'active' : ''}" href="${cp}/admin/compras"><t:icone nome="recibo"/>Compras</a>
<a class="${menu == 'listagens' ? 'active' : ''}" href="${cp}/admin/listagens"><t:icone nome="lista"/>Listagens</a>
<a class="${menu == 'relatorios' ? 'active' : ''}" href="${cp}/admin/relatorios"><t:icone nome="grafico"/>Relatórios</a>
<a class="${menu == 'persistencia' ? 'active' : ''}" href="${cp}/admin/persistencia"><t:icone nome="guardar"/>Guardar dados</a>
</c:when><c:otherwise>
<a class="${menu == 'catalogo' ? 'active' : ''}" href="${cp}/catalogo"><t:icone nome="folha"/>Catálogo</a>
<a class="${menu == 'carrinho' ? 'active' : ''}" href="${cp}/carrinho"><t:icone nome="carrinho"/>Meu carrinho <span class="nav-count">${cartCount}</span></a>
<a class="${menu == 'compras' ? 'active' : ''}" href="${cp}/minhas-compras"><t:icone nome="recibo"/>Minhas compras</a>
</c:otherwise></c:choose></nav>
<div class="sidebar-bottom"><div class="mini-field"><t:icone nome="campo"/><strong>Da terra ao futuro.</strong><span>Uma gestão que dá frutos.</span></div><div class="sidebar-note">AgroGest · Nampula, Moçambique<br>Gestão agrícola integrada</div></div></aside>
<div class="workspace"><header class="topbar"><button class="menu-toggle btn secondary small" type="button" aria-label="Abrir menu" aria-controls="navegacao" aria-expanded="false"><t:icone nome="lista"/></button><div class="breadcrumb">AgroGest <span>/</span> <strong><c:out value="${titulo}"/></strong></div><div class="user"><span class="avatar"><t:icone nome="pessoa"/></span><div><strong><c:out value="${utilizador.nome}"/></strong><small>${utilizador.perfil == 'ADMIN' ? 'Administrador' : 'Cliente'}</small></div><form method="post" action="${cp}/logout"><t:csrf/><button class="logout" title="Terminar sessão" aria-label="Terminar sessão"><t:icone nome="sair"/></button></form></div></header>
<main id="conteudo" tabindex="-1"><c:if test="${not empty sucesso}"><div class="alert success" role="status"><t:icone nome="certo"/><c:out value="${sucesso}"/></div></c:if><c:if test="${not empty erro}"><div class="alert error" role="alert"><c:out value="${erro}"/></div></c:if><jsp:doBody/></main>
<footer class="page-footer"><span>AgroGest · Gestão agrícola com clareza</span><span>Moçambique</span></footer></div></div></body></html>
