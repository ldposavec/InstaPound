<script lang="ts">
  import { push } from 'svelte-spa-router';
  import { Camera, Upload, Search, Users, Shield, Sparkles, ArrowRight, Image, Download } from 'lucide-svelte';
  import { authStore } from '../stores/index.svelte';

  const features = [
    {
      icon: Upload,
      title: 'Easy Upload',
      description: 'Upload photos with custom processing options, hashtags, and descriptions.'
    },
    {
      icon: Search,
      title: 'Smart Search',
      description: 'Find photos by hashtags, author, date range, and file size.'
    },
    {
      icon: Download,
      title: 'Flexible Downloads',
      description: 'Download original or apply filters like resize, sepia, and blur.'
    },
    {
      icon: Users,
      title: 'User Tiers',
      description: 'Choose FREE, PRO, or GOLD packages with different limits.'
    },
    {
      icon: Shield,
      title: 'Admin Controls',
      description: 'Comprehensive admin panel for user and content management.'
    },
    {
      icon: Sparkles,
      title: 'Image Processing',
      description: 'Convert formats, resize images, and apply effects before saving.'
    }
  ];
</script>

<div class="min-h-screen bg-gradient-to-br from-violet-50 via-white to-purple-50 dark:from-gray-900 dark:via-gray-900 dark:to-violet-950">
  <!-- Hero Section -->
  <section class="relative overflow-hidden">
    <div class="absolute inset-0 bg-gradient-to-r from-violet-600/10 to-purple-600/10 dark:from-violet-600/5 dark:to-purple-600/5"></div>
    <div class="absolute top-20 left-10 w-72 h-72 bg-violet-400 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob"></div>
    <div class="absolute top-40 right-10 w-72 h-72 bg-purple-400 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob animation-delay-2000"></div>
    <div class="absolute bottom-20 left-1/2 w-72 h-72 bg-pink-400 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob animation-delay-4000"></div>
    
    <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-32">
      <div class="text-center">
        <div class="inline-flex items-center gap-2 px-4 py-2 bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 rounded-full text-sm font-medium mb-6">
          <Camera class="w-4 h-4" />
          Photo Management Made Beautiful
        </div>
        
        <h1 class="text-4xl md:text-6xl lg:text-7xl font-bold text-gray-900 dark:text-white mb-6 leading-tight">
          Share Your
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-violet-600 to-purple-600">
            Visual Stories
          </span>
        </h1>
        
        <p class="text-lg md:text-xl text-gray-600 dark:text-gray-300 max-w-2xl mx-auto mb-10">
          Upload, organize, and share your photos with powerful processing options.
          Choose your package and start your creative journey today.
        </p>
        
        <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
          {#if authStore.isAuthenticated}
            <button
              onclick={() => push('/upload')}
              class="group flex items-center gap-2 px-8 py-4 bg-violet-600 hover:bg-violet-700 text-white rounded-xl font-semibold text-lg shadow-lg shadow-violet-500/30 hover:shadow-violet-500/40 transition-all"
            >
              <Upload class="w-5 h-5" />
              Upload Photo
              <ArrowRight class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </button>
            <button
              onclick={() => push('/browse')}
              class="flex items-center gap-2 px-8 py-4 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 text-gray-900 dark:text-white rounded-xl font-semibold text-lg border border-gray-200 dark:border-gray-700 transition-colors"
            >
              <Image class="w-5 h-5" />
              Browse Gallery
            </button>
          {:else}
            <button
              onclick={() => push('/register')}
              class="group flex items-center gap-2 px-8 py-4 bg-violet-600 hover:bg-violet-700 text-white rounded-xl font-semibold text-lg shadow-lg shadow-violet-500/30 hover:shadow-violet-500/40 transition-all"
            >
              Get Started Free
              <ArrowRight class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </button>
            <button
              onclick={() => push('/browse')}
              class="flex items-center gap-2 px-8 py-4 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 text-gray-900 dark:text-white rounded-xl font-semibold text-lg border border-gray-200 dark:border-gray-700 transition-colors"
            >
              <Image class="w-5 h-5" />
              Browse Gallery
            </button>
          {/if}
        </div>
      </div>
    </div>
  </section>

  <!-- Features Section -->
  <section class="py-20 md:py-28">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="text-center mb-16">
        <h2 class="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">
          Everything You Need
        </h2>
        <p class="text-lg text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
          Powerful features to help you manage, organize, and share your photos effortlessly.
        </p>
      </div>
      
      <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
        {#each features as feature}
          <div class="group p-6 bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 hover:border-violet-200 dark:hover:border-violet-800 hover:shadow-lg hover:shadow-violet-500/10 transition-all">
            <div class="w-12 h-12 bg-violet-100 dark:bg-violet-900/30 rounded-xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
              <svelte:component this={feature.icon} class="w-6 h-6 text-violet-600 dark:text-violet-400" />
            </div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">
              {feature.title}
            </h3>
            <p class="text-gray-600 dark:text-gray-400">
              {feature.description}
            </p>
          </div>
        {/each}
      </div>
    </div>
  </section>

  <!-- Pricing Section -->
  <section class="py-20 md:py-28 bg-gray-50 dark:bg-gray-800/50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="text-center mb-16">
        <h2 class="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">
          Choose Your Plan
        </h2>
        <p class="text-lg text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
          Start free and upgrade as you grow. Change your plan once per day.
        </p>
      </div>
      
      <div class="grid md:grid-cols-3 gap-8 max-w-5xl mx-auto">
        <!-- FREE -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-8">
          <div class="text-center mb-8">
            <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-2">FREE</h3>
            <div class="text-4xl font-bold text-gray-900 dark:text-white">
              $0
              <span class="text-lg font-normal text-gray-500">/month</span>
            </div>
          </div>
          <ul class="space-y-3 mb-8">
            <li class="flex items-center gap-3 text-gray-600 dark:text-gray-400">
              <span class="w-5 h-5 bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 rounded-full flex items-center justify-center text-xs">✓</span>
              5MB max upload size
            </li>
            <li class="flex items-center gap-3 text-gray-600 dark:text-gray-400">
              <span class="w-5 h-5 bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 rounded-full flex items-center justify-center text-xs">✓</span>
              5 uploads per day
            </li>
            <li class="flex items-center gap-3 text-gray-600 dark:text-gray-400">
              <span class="w-5 h-5 bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 rounded-full flex items-center justify-center text-xs">✓</span>
              50 photos storage
            </li>
          </ul>
          <button
            onclick={() => push('/register')}
            class="w-full py-3 border-2 border-violet-600 text-violet-600 dark:text-violet-400 hover:bg-violet-50 dark:hover:bg-violet-900/20 rounded-xl font-semibold transition-colors"
          >
            Get Started
          </button>
        </div>

        <!-- PRO -->
        <div class="bg-violet-600 rounded-2xl p-8 relative overflow-hidden shadow-xl shadow-violet-500/30 transform md:-translate-y-4">
          <div class="absolute top-0 right-0 bg-yellow-400 text-yellow-900 text-xs font-bold px-3 py-1 rounded-bl-lg">
            POPULAR
          </div>
          <div class="text-center mb-8">
            <h3 class="text-xl font-bold text-white mb-2">PRO</h3>
            <div class="text-4xl font-bold text-white">
              $9.99
              <span class="text-lg font-normal text-violet-200">/month</span>
            </div>
          </div>
          <ul class="space-y-3 mb-8">
            <li class="flex items-center gap-3 text-violet-100">
              <span class="w-5 h-5 bg-white/20 text-white rounded-full flex items-center justify-center text-xs">✓</span>
              25MB max upload size
            </li>
            <li class="flex items-center gap-3 text-violet-100">
              <span class="w-5 h-5 bg-white/20 text-white rounded-full flex items-center justify-center text-xs">✓</span>
              25 uploads per day
            </li>
            <li class="flex items-center gap-3 text-violet-100">
              <span class="w-5 h-5 bg-white/20 text-white rounded-full flex items-center justify-center text-xs">✓</span>
              500 photos storage
            </li>
          </ul>
          <button
            onclick={() => push('/register')}
            class="w-full py-3 bg-white text-violet-600 hover:bg-violet-50 rounded-xl font-semibold transition-colors"
          >
            Start Pro Trial
          </button>
        </div>

        <!-- GOLD -->
        <div class="bg-gradient-to-br from-amber-50 to-yellow-50 dark:from-amber-900/20 dark:to-yellow-900/20 rounded-2xl border-2 border-amber-200 dark:border-amber-800 p-8">
          <div class="text-center mb-8">
            <h3 class="text-xl font-bold text-amber-800 dark:text-amber-200 mb-2">GOLD</h3>
            <div class="text-4xl font-bold text-amber-800 dark:text-amber-200">
              $29.99
              <span class="text-lg font-normal text-amber-600 dark:text-amber-400">/month</span>
            </div>
          </div>
          <ul class="space-y-3 mb-8">
            <li class="flex items-center gap-3 text-amber-700 dark:text-amber-300">
              <span class="w-5 h-5 bg-amber-200 dark:bg-amber-800 text-amber-700 dark:text-amber-200 rounded-full flex items-center justify-center text-xs">✓</span>
              100MB max upload size
            </li>
            <li class="flex items-center gap-3 text-amber-700 dark:text-amber-300">
              <span class="w-5 h-5 bg-amber-200 dark:bg-amber-800 text-amber-700 dark:text-amber-200 rounded-full flex items-center justify-center text-xs">✓</span>
              100 uploads per day
            </li>
            <li class="flex items-center gap-3 text-amber-700 dark:text-amber-300">
              <span class="w-5 h-5 bg-amber-200 dark:bg-amber-800 text-amber-700 dark:text-amber-200 rounded-full flex items-center justify-center text-xs">✓</span>
              5000 photos storage
            </li>
          </ul>
          <button
            onclick={() => push('/register')}
            class="w-full py-3 bg-gradient-to-r from-amber-500 to-yellow-500 hover:from-amber-600 hover:to-yellow-600 text-white rounded-xl font-semibold transition-all"
          >
            Go Gold
          </button>
        </div>
      </div>
    </div>
  </section>

  <!-- CTA Section -->
  <section class="py-20 md:py-28">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
      <h2 class="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-6">
        Ready to Start Sharing?
      </h2>
      <p class="text-lg text-gray-600 dark:text-gray-400 mb-10">
        Join thousands of photographers who trust InstaPound to manage their visual content.
      </p>
      <button
        onclick={() => push(authStore.isAuthenticated ? '/upload' : '/register')}
        class="group inline-flex items-center gap-2 px-8 py-4 bg-violet-600 hover:bg-violet-700 text-white rounded-xl font-semibold text-lg shadow-lg shadow-violet-500/30 hover:shadow-violet-500/40 transition-all"
      >
        {authStore.isAuthenticated ? 'Upload Your First Photo' : 'Create Free Account'}
        <ArrowRight class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
      </button>
    </div>
  </section>
</div>

<style>
  @keyframes blob {
    0%, 100% {
      transform: translate(0, 0) scale(1);
    }
    33% {
      transform: translate(30px, -50px) scale(1.1);
    }
    66% {
      transform: translate(-20px, 20px) scale(0.9);
    }
  }
  
  .animate-blob {
    animation: blob 7s infinite;
  }
  
  .animation-delay-2000 {
    animation-delay: 2s;
  }
  
  .animation-delay-4000 {
    animation-delay: 4s;
  }
</style>
