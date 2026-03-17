/**
 * Claw-Test 导航配置
 *
 * 注意：新版本使用 layout.js 提供侧边导航
 * 此文件仅作为配置导出，供需要独立使用导航配置的页面使用
 */

// 导航配置（供外部引用）
const NAV_CONFIG = {
    items: [
        { href: '/tasks.html', label: '任务管理', icon: '📋' },
        { href: '/identities.html', label: '身份管理', icon: '🔐' },
        { href: '/projects.html', label: '项目管理', icon: '📁' },
        { href: '/design-docs.html', label: '设计文档', icon: '📄' }
    ],
    defaultApiKey: ''
};

// 导出配置供其他脚本使用
if (typeof window !== 'undefined') {
    window.NAV_CONFIG = NAV_CONFIG;
}
