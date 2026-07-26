"""
Entry-point for `python run.py` or `uvicorn run:app`.
"""

import uvicorn

from app.config import PORT
from app.main import app  # noqa: F401 – re-exported for uvicorn

if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=PORT,
        reload=True,
        log_level="info",
    )
