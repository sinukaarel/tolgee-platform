import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import viteTsconfigPaths from 'vite-tsconfig-paths';
import svgr from 'vite-plugin-svgr';
import { nodePolyfills } from 'vite-plugin-node-polyfills';
import mdx from '@mdx-js/rollup';
import { viteStaticCopy } from 'vite-plugin-static-copy';
import { resolve } from 'path';

import { extractDataCy } from './dataCy.plugin';
import rehypeHighlight from 'rehype-highlight';
import { loadEnv } from 'vite';

export default defineConfig(({ mode }) => {
  process.env = { ...process.env, ...loadEnv(mode, process.cwd()) };
  return {
    // depending on your application, base can also be "/"
    base: '/',
    plugins: [
      react(),
      viteTsconfigPaths(),
      svgr(),
      mdx({ rehypePlugins: [rehypeHighlight] }),
      nodePolyfills(),
      extractDataCy(),
      viteStaticCopy({
        targets: [
          {
            src: resolve('node_modules/@tginternal/language-util/flags'),
            dest: '',
          },
        ],
      }),
    ],
    server: {
      // this ensures that the browser opens upon server start
      open: true,
      // this sets a default port to 3000
      port: Number(process.env.VITE_PORT) || 3000,
    },
  };
});
