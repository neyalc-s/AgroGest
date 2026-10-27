#!/usr/bin/env python3
"""Navegação real no menu: requer Node.js, Playwright e Chromium."""
from pathlib import Path
import os
import shutil
import subprocess
import tempfile
import test_http as h


def main():
    node = os.environ.get('CODEX_PRIMARY_RUNTIME_NODE') or shutil.which('node')
    if not node:
        raise RuntimeError('Instale Node.js e Playwright para este teste opcional.')
    process = None
    with tempfile.TemporaryDirectory(prefix='agrogest-navegacao-') as tmp:
        folder = Path(tmp)
        with (folder/'tomcat.log').open('w') as log:
            try:
                process = h.start(folder/'dados', log)
                result = subprocess.run([node, str(Path(__file__).with_suffix('.cjs'))],
                    check=True, timeout=180, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                print(result.stdout, end='', flush=True)
                if '"resultado":"PASSOU"' not in result.stdout:
                    raise RuntimeError('O teste não chegou à verificação final.')
            except subprocess.CalledProcessError as error:
                print(error.stdout or '', flush=True)
                log.flush()
                print((folder/'tomcat.log').read_text()[-10000:])
                raise
            finally:
                if process is not None and process.poll() is None:
                    h.stop(process)


if __name__ == '__main__':
    main()
