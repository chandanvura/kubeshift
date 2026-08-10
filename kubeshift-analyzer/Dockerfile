FROM python:3.12-slim

# Create a non-root user
RUN useradd -m -s /bin/bash analyzeruser

WORKDIR /app

# Install dependencies first for better caching
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY analyzer/ analyzer/

# Ensure proper ownership
RUN chown -R analyzeruser:analyzeruser /app

USER analyzeruser

EXPOSE 8082

CMD ["uvicorn", "analyzer.main:app", "--host", "0.0.0.0", "--port", "8082"]
