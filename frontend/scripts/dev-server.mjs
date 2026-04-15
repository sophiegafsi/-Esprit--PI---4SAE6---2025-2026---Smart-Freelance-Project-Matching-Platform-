import { createServer } from 'node:http';
import { readFile } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(__dirname, '..');
const requestedPort = Number.parseInt(process.env.PORT || '4200', 10);

const mimeTypes = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.ico': 'image/x-icon'
};

function resolveRequest(url) {
  const cleanUrl = decodeURIComponent(new URL(url, 'http://localhost').pathname);
  const relativePath = cleanUrl === '/' ? 'index.html' : cleanUrl.slice(1);
  const filePath = path.resolve(rootDir, relativePath);

  if (!filePath.startsWith(rootDir)) {
    return null;
  }

  if (existsSync(filePath)) {
    return filePath;
  }

  return path.resolve(rootDir, 'index.html');
}

function startServer(port) {
  const server = createServer(async (req, res) => {
    try {
      const filePath = resolveRequest(req.url || '/');
      if (!filePath) {
        res.writeHead(403);
        res.end('Forbidden');
        return;
      }

      const data = await readFile(filePath);
      const extension = path.extname(filePath).toLowerCase();
      res.writeHead(200, {
        'Content-Type': mimeTypes[extension] || 'application/octet-stream',
        'Cache-Control': 'no-store'
      });
      res.end(data);
    } catch (error) {
      res.writeHead(error.code === 'ENOENT' ? 404 : 500, {
        'Content-Type': 'text/plain; charset=utf-8'
      });
      res.end(error.code === 'ENOENT' ? 'Not found' : error.message);
    }
  });

  server.on('error', (error) => {
    if (error.code === 'EADDRINUSE') {
      console.log(`Port ${port} deja utilise, essai sur ${port + 1}...`);
      startServer(port + 1);
      return;
    }
    throw error;
  });

  server.listen(port, () => {
    console.log(`Frontend evaluation/recompense pret sur http://localhost:${port}`);
  });
}

startServer(requestedPort);
