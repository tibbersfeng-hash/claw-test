/**
 * Claw-Test 布局系统
 * 提供侧边导航、主题切换等全局功能
 */

// 导航配置
const NAV_CONFIG = {
    brand: {
        name: 'Claw Test',
        icon: '🐾',
        href: '/'
    },
    items: [
        { href: '/tasks.html', label: '任务管理', icon: '📋' },
        { href: '/identities.html', label: '身份管理', icon: '🔐' },
        { href: '/projects.html', label: '项目管理', icon: '📁' },
        { href: '/design-docs.html', label: '设计文档', icon: '📄' }
    ],
    defaultApiKey: 'sk-9800eb048ad0446b8030e262efad9498'
};

// 初始化主题
function initTheme() {
    const savedTheme = localStorage.getItem('claw-theme');
    if (savedTheme) {
        document.documentElement.setAttribute('data-theme', savedTheme);
    } else if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
        document.documentElement.setAttribute('data-theme', 'dark');
    }
}

// 切换主题
function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('claw-theme', newTheme);

    const themeBtn = document.querySelector('.theme-toggle');
    if (themeBtn) {
        const icon = themeBtn.querySelector('.theme-icon');
        const text = themeBtn.querySelector('.theme-text');
        if (icon) icon.textContent = newTheme === 'dark' ? '☀️' : '🌙';
        if (text) text.textContent = newTheme === 'dark' ? '浅色模式' : '深色模式';
    }
}

// 获取当前页面路径对应的导航项
function getActiveNavItem() {
    const currentPath = window.location.pathname;
    return NAV_CONFIG.items.find(item => currentPath === item.href ||
        (item.href === '/tasks.html' && (currentPath === '/' || currentPath === '/index.html')));
}

// 切换侧边栏（移动端）
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    if (sidebar) sidebar.classList.toggle('open');
    if (overlay) overlay.classList.toggle('active');
}

// 关闭侧边栏（移动端）
function closeSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    if (sidebar) sidebar.classList.remove('open');
    if (overlay) overlay.classList.remove('active');
}

// 初始化布局
function initLayout() {
    initTheme();

    const activeItem = getActiveNavItem();
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';

    // 创建侧边栏
    const sidebar = document.createElement('aside');
    sidebar.className = 'sidebar';
    sidebar.id = 'sidebar';
    sidebar.innerHTML = `
        <div class="sidebar-brand">
            <a href="${NAV_CONFIG.brand.href}" class="brand-link">
                <h1>
                    <span class="logo-icon">${NAV_CONFIG.brand.icon}</span>
                    <span>${NAV_CONFIG.brand.name}</span>
                </h1>
            </a>
        </div>
        <nav class="sidebar-nav">
            ${NAV_CONFIG.items.map(item => `
                <a href="${item.href}" class="nav-item ${activeItem === item ? 'active' : ''}">
                    <span class="nav-icon">${item.icon}</span>
                    <span class="nav-label">${item.label}</span>
                </a>
            `).join('')}
        </nav>
        <div class="sidebar-footer">
            <button class="theme-toggle" id="themeToggleBtn">
                <span class="theme-icon">${isDark ? '☀️' : '🌙'}</span>
                <span class="theme-text">${isDark ? '浅色模式' : '深色模式'}</span>
            </button>
        </div>
    `;

    // 创建遮罩层
    const overlay = document.createElement('div');
    overlay.className = 'sidebar-overlay';
    overlay.id = 'sidebarOverlay';

    // 创建移动端菜单按钮
    const menuBtn = document.createElement('button');
    menuBtn.className = 'mobile-menu-btn';
    menuBtn.id = 'mobileMenuBtn';
    menuBtn.innerHTML = `
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12h18M3 6h18M3 18h18"/>
        </svg>
    `;

    // 创建应用容器
    const appContainer = document.createElement('div');
    appContainer.className = 'app-container';

    // 创建主内容区域
    const mainContent = document.createElement('main');
    mainContent.className = 'main-content';

    // 在 body 开头插入元素
    document.body.insertBefore(sidebar, document.body.firstChild);
    document.body.insertBefore(overlay, document.body.firstChild.nextSibling);
    document.body.insertBefore(menuBtn, document.body.firstChild.nextSibling.nextSibling);

    // 给 body 添加类用于 CSS
    document.body.classList.add('has-sidebar');

    // 绑定事件
    document.getElementById('themeToggleBtn').addEventListener('click', toggleTheme);
    overlay.addEventListener('click', closeSidebar);
    menuBtn.addEventListener('click', toggleSidebar);

    // 移除旧的导航
    const oldNav = document.querySelector('.nav');
    if (oldNav) oldNav.remove();

    // 转换标题
    const oldH1 = document.querySelector('body > h1:first-of-type');
    if (oldH1) {
        const icon = oldH1.textContent.match(/^[\u{1F300}-\u{1F9FF}]/u)?.[0] || '';
        const title = oldH1.textContent.replace(/^[\u{1F300}-\u{1F9FF}]\s*/u, '');
        const pageHeader = document.createElement('div');
        pageHeader.className = 'page-header';
        pageHeader.innerHTML = `<h1 class="page-title">${icon ? `<span class="icon">${icon}</span>` : ''}${title}</h1>`;
        oldH1.replaceWith(pageHeader);
    }
}

// 页面加载完成后初始化布局
document.addEventListener('DOMContentLoaded', initLayout);

// 导出
window.ClawConfig = { NAV_CONFIG, toggleTheme, closeSidebar };
