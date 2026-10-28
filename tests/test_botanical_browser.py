#!/usr/bin/env python3
"""Visual and responsive regression checks. Uses isolated data and the existing Tomcat runner."""
from pathlib import Path
import os
import shutil
import subprocess
import tempfile
import test_http as h


def main():
    node = os.environ.get('CODEX_PRIMARY_RUNTIME_NODE') or shutil.which('node')
    if not node:
        raise RuntimeError('Node.js and Playwright are required for browser verification.')
    process = None
    with tempfile.TemporaryDirectory(prefix='agrogest-botanical-') as tmp:
        folder = Path(tmp)
        with (folder / 'tomcat.log').open('w') as log:
            try:
                process = h.start(folder / 'dados', log)
                subprocess.run([node, str(Path(__file__).with_suffix('.cjs'))], check=True, timeout=240)
            except Exception:
                log.flush()
                print((folder / 'tomcat.log').read_text()[-10000:])
                raise
            finally:
                if process is not None and process.poll() is None:
                    h.stop(process)


if __name__ == '__main__':
    main()
