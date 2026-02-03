<script lang="ts">
  import { push } from 'svelte-spa-router';
  import { Mail, Lock, Loader2, Github, Chrome } from 'lucide-svelte';
  import { authService } from '../services/api';
  import { toastStore } from '../stores/index.svelte';
  import type { LoginCredentials } from '../types';

  let credentials = $state<LoginCredentials>({
    email: '',
    password: ''
  });
  
  let isLoading = $state(false);
  let isGoogleLoading = $state(false);
  let isGithubLoading = $state(false);

  async function handleSubmit(e: SubmitEvent) {
    e.preventDefault();
    
    if (!credentials.email || !credentials.password) {
      toastStore.error('Please fill in all fields');
      return;
    }
    
    isLoading = true;
    
    try {
      const result = await authService.login(credentials);
      
      if (result.success) {
        push('/dashboard');
      } else {
        toastStore.error(result.error || 'Login failed');
      }
    } finally {
      isLoading = false;
    }
  }

  async function handleGoogleLogin() {
    isGoogleLoading = true;
    
    try {
      const result = await authService.loginWithGoogle();
      
      if (result.success) {
        push('/dashboard');
      } else {
        toastStore.error(result.error || 'Google login failed');
      }
    } finally {
      isGoogleLoading = false;
    }
  }

  async function handleGithubLogin() {
    isGithubLoading = true;
    
    try {
      const result = await authService.loginWithGithub();
      
      if (result.success) {
        push('/dashboard');
      } else {
        toastStore.error(result.error || 'GitHub login failed');
      }
    } finally {
      isGithubLoading = false;
    }
  }
</script>

<div class="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-gray-50 dark:bg-gray-900">
  <div class="max-w-md w-full">
    <!-- Header -->
    <div class="text-center mb-8">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white">
        Welcome Back
      </h1>
      <p class="mt-2 text-gray-600 dark:text-gray-400">
        Sign in to your account to continue
      </p>
    </div>

    <!-- Login Form -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-8">
      <!-- Social Login Buttons -->
      <div class="space-y-3 mb-6">
        <button
          onclick={handleGoogleLogin}
          disabled={isGoogleLoading || isLoading || isGithubLoading}
          class="w-full flex items-center justify-center gap-3 px-4 py-3 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600 rounded-xl font-medium text-gray-700 dark:text-gray-200 transition-colors disabled:opacity-50"
        >
          {#if isGoogleLoading}
            <Loader2 class="w-5 h-5 animate-spin" />
          {:else}
            <Chrome class="w-5 h-5 text-red-500" />
          {/if}
          Continue with Google
        </button>
        
        <button
          onclick={handleGithubLogin}
          disabled={isGithubLoading || isLoading || isGoogleLoading}
          class="w-full flex items-center justify-center gap-3 px-4 py-3 bg-gray-900 dark:bg-gray-600 hover:bg-gray-800 dark:hover:bg-gray-500 text-white rounded-xl font-medium transition-colors disabled:opacity-50"
        >
          {#if isGithubLoading}
            <Loader2 class="w-5 h-5 animate-spin" />
          {:else}
            <Github class="w-5 h-5" />
          {/if}
          Continue with GitHub
        </button>
      </div>

      <!-- Divider -->
      <div class="relative mb-6">
        <div class="absolute inset-0 flex items-center">
          <div class="w-full border-t border-gray-200 dark:border-gray-700"></div>
        </div>
        <div class="relative flex justify-center text-sm">
          <span class="px-4 bg-white dark:bg-gray-800 text-gray-500">or continue with email</span>
        </div>
      </div>

      <!-- Email/Password Form -->
      <form onsubmit={handleSubmit} class="space-y-5">
        <div>
          <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Email Address
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <Mail class="h-5 w-5 text-gray-400" />
            </div>
            <input
              type="email"
              id="email"
              bind:value={credentials.email}
              placeholder="you@example.com"
              class="w-full pl-11 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-all"
              required
            />
          </div>
        </div>

        <div>
          <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Password
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <Lock class="h-5 w-5 text-gray-400" />
            </div>
            <input
              type="password"
              id="password"
              bind:value={credentials.password}
              placeholder="••••••••"
              class="w-full pl-11 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-all"
              required
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={isLoading || isGoogleLoading || isGithubLoading}
          class="w-full flex items-center justify-center gap-2 px-4 py-3 bg-violet-600 hover:bg-violet-700 disabled:bg-violet-400 text-white rounded-xl font-semibold transition-colors"
        >
          {#if isLoading}
            <Loader2 class="w-5 h-5 animate-spin" />
            Signing in...
          {:else}
            Sign In
          {/if}
        </button>
      </form>

      <!-- Demo credentials hint -->
      <div class="mt-6 p-4 bg-violet-50 dark:bg-violet-900/20 rounded-xl">
        <p class="text-sm text-violet-700 dark:text-violet-300 font-medium mb-2">Demo Credentials:</p>
        <p class="text-xs text-violet-600 dark:text-violet-400">
          Admin: admin@instapound.com / admin<br/>
          User: user@example.com / user
        </p>
      </div>
    </div>

    <!-- Sign up link -->
    <p class="mt-6 text-center text-gray-600 dark:text-gray-400">
      Don't have an account?
      <button
        onclick={() => push('/register')}
        class="text-violet-600 dark:text-violet-400 hover:text-violet-700 dark:hover:text-violet-300 font-medium ml-1"
      >
        Sign up
      </button>
    </p>
  </div>
</div>
