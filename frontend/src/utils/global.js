// 为浏览器环境提供global对象，解决SockJS依赖问题
if (typeof window !== 'undefined' && !window.global) {
  window.global = window;
}

export default window.global;
