<script lang="ts">
  import { X, CheckCircle, AlertCircle, Info, AlertTriangle } from 'lucide-svelte';
  import { toastStore, type ToastType } from '../stores/index.svelte';

  const iconMap: Record<ToastType, typeof CheckCircle> = {
    success: CheckCircle,
    error: AlertCircle,
    info: Info,
    warning: AlertTriangle
  };

  const colorMap: Record<ToastType, string> = {
    success: 'bg-green-500',
    error: 'bg-red-500',
    info: 'bg-blue-500',
    warning: 'bg-yellow-500'
  };
</script>

<div class="fixed top-4 right-4 z-50 flex flex-col gap-2 max-w-sm">
  {#each toastStore.toasts as toast (toast.id)}
    <div 
      class="{colorMap[toast.type]} text-white p-4 rounded-lg shadow-lg flex items-center gap-3 animate-slide-in"
      role="alert"
    >
      <svelte:component this={iconMap[toast.type]} class="w-5 h-5 flex-shrink-0" />
      <p class="flex-1 text-sm font-medium">{toast.message}</p>
      <button 
        onclick={() => toastStore.remove(toast.id)}
        class="hover:opacity-80 transition-opacity"
        aria-label="Dismiss"
      >
        <X class="w-4 h-4" />
      </button>
    </div>
  {/each}
</div>

<style>
  @keyframes slide-in {
    from {
      opacity: 0;
      transform: translateX(100%);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }
  
  .animate-slide-in {
    animation: slide-in 0.3s ease-out;
  }
</style>
