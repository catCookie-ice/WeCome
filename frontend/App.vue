<template>

  <!-- 用 transition 包裹路由出口，name 用于定义过渡类名前缀 -->
  <transition name="route">
      <RouterView />
  </transition> 
</template>


<!-- <script setup>
import { onMounted, onBeforeUnmount } from 'vue';

// 标记是否为刷新操作
let isRefreshing = false;

// 清除登录缓存（针对实际存储的键名 login-user）
const clearLoginCache = () => {
  localStorage.removeItem('login-user');
};

// 页面加载时触发（刷新会执行，关闭不会）
const handleLoad = () => {
  isRefreshing = true; // 刷新时标记为 true
};

// 关键修改：用 unload 事件替代 beforeunload
const handleUnload = () => {
  // 只有关闭网页（非刷新）时清除缓存
  if (!isRefreshing) {
    clearLoginCache();
  }
  isRefreshing = false;
};

// // 页面卸载前触发（关闭/刷新都会执行）
// const handleBeforeUnload = () => {
//   // 只有当不是刷新（即关闭网页）时，才清除缓存
//   if (!isRefreshing) {
//     clearLoginCache();
//   }
//   // 重置标记，避免影响下次判断
//   isRefreshing = false;
// };


//关闭网页：页面会直接进入「销毁」状态，不会再次触发「加载」事件。
// 刷新页面：页面会先销毁再重新加载，会触发「加载」事件。
// 组件挂载时绑定事件

// 执行时机和事件本身的 “发生阶段” 强相关，结论是：函数在事件对应的 “关键阶段” 触发时执行，而非单纯的 “事件发生前” 或 “之后”。
onMounted(() => {
  // 监听页面加载事件（用于判断是否为刷新）
  window.addEventListener('load', handleLoad);//执行时机：在 “页面加载完成后” 执行。
  // 监听页面卸载事件（用于判断是否为关闭）
  // window.addEventListener('beforeunload', handleBeforeUnload);//在 “页面即将卸载前” 执行（属于事件发生前的 “预处理阶段”）。
  window.addEventListener('unload', handleUnload);//unload 事件触发时机在 beforeunload 之后、页面真正关闭前，且浏览器会等待其内部同步操作完成，能确保缓存清除成功。
});

// 组件卸载时解绑事件（避免内存泄漏）
onBeforeUnmount(() => {
  window.removeEventListener('load', handleLoad);
  window.removeEventListener('beforeunload', handleBeforeUnload);
});
</script>  -->
*/



<style>
 /* 定义路由切换过渡效果 */
  .route-enter-active, .route-leave-active {
    transition: all 0.3s ease;
  }
  .route-enter-from, .route-leave-to {
    opacity: 0;
    transform: translateX(30px);
  }
</style>

