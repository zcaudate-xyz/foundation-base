import { defineConfig } from '@neon/config/v1';

export default defineConfig({
  auth: true,
  dataApi: true,
  preview: { realtime: { allowedOrigins: ['http://localhost:18787', 'http://localhost:18788'] } },
  branch: () => ({ postgres: { computeSettings: {
    autoscalingLimitMinCu: 0.25,
    autoscalingLimitMaxCu: 0.25,
  } } }),
});
