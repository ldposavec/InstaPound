<script lang="ts">
  import Router from 'svelte-spa-router';
  import Navbar from './components/Navbar.svelte';
  import Toast from './components/Toast.svelte';
  import Home from './pages/Home.svelte';
  import Login from './pages/Login.svelte';
  import Register from './pages/Register.svelte';
  import Browse from './pages/Browse.svelte';
  import Search from './pages/Search.svelte';
  import Upload from './pages/Upload.svelte';
  import Dashboard from './pages/Dashboard.svelte';
  import Admin from './pages/Admin.svelte';

  const routes = {
    '/': Home,
    '/login': Login,
    '/register': Register,
    '/browse': Browse,
    '/search': Search,
    '/upload': Upload,
    '/dashboard': Dashboard,
    '/admin': Admin
  };
</script>

<div class="min-h-screen bg-gray-50 dark:bg-gray-900">
  <Navbar />
  <main>
    <Router {routes} />
  </main>
  <Toast />
</div>

