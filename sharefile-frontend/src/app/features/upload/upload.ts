import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './upload.html',
  styleUrls: ['./upload.scss']
})
export class UploadComponent {
  files: File[] = [];
  recipients: string[] = [];
  currentEmail: string = '';
  message: string = '';
  emailError: string = '';

  isDragOver: boolean = false;
  isUploading: boolean = false;
  uploadProgress: number = 0;

  readonly MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB in bytes

  constructor(private router: Router) {}

  // Drag and Drop handlers
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;

    const files = event.dataTransfer?.files;
    if (files) {
      this.handleFiles(files);
    }
  }

  // File selection handler
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.handleFiles(input.files);
    }
  }

  // Process selected files
  private handleFiles(fileList: FileList): void {
    const newFiles = Array.from(fileList);

    // Validate file sizes
    const invalidFiles = newFiles.filter(file => file.size > this.MAX_FILE_SIZE);

    if (invalidFiles.length > 0) {
      alert(`Some files exceed the 500MB limit:\n${invalidFiles.map(f => f.name).join('\n')}`);
      return;
    }

    // Add valid files
    const validFiles = newFiles.filter(file => file.size <= this.MAX_FILE_SIZE);
    this.files.push(...validFiles);
  }

  // Remove file from list
  removeFile(index: number): void {
    this.files.splice(index, 1);
  }

  // Format file size for display
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  // Get total size of all files
  getTotalSize(): string {
    const total = this.files.reduce((sum, file) => sum + file.size, 0);
    return this.formatFileSize(total);
  }

  // Email validation
  isValidEmail(email: string): boolean {
    if (!email || email.trim() === '') return false;

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email.trim());
  }

  // Add recipient
  addRecipient(): void {
    this.emailError = '';
    const email = this.currentEmail.trim().toLowerCase();

    if (!this.isValidEmail(email)) {
      this.emailError = 'Please enter a valid email address';
      return;
    }

    if (this.recipients.includes(email)) {
      this.emailError = 'This email has already been added';
      return;
    }

    this.recipients.push(email);
    this.currentEmail = '';
  }

  // Remove recipient
  removeRecipient(index: number): void {
    this.recipients.splice(index, 1);
    this.emailError = '';
  }

  // Create and upload transfer
  async createTransfer(): Promise<void> {
    if (this.files.length === 0) {
      alert('Please add at least one file');
      return;
    }

    if (this.recipients.length === 0) {
      alert('Please add at least one recipient');
      return;
    }

    this.isUploading = true;
    this.uploadProgress = 0;

    try {
      // Step 1: Initialize transfer
      const transferData = {
        recipients: this.recipients,
        message: this.message || null,
        expiresInDays: 7
      };

      const initResponse = await fetch('/api/transfers', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify(transferData)
      });

      if (!initResponse.ok) {
        throw new Error('Failed to initialize transfer');
      }

      const transfer = await initResponse.json();
      const transferId = transfer.id;

      // Step 2: Upload each file
      const totalFiles = this.files.length;

      for (let i = 0; i < this.files.length; i++) {
        const file = this.files[i];

        // Calculate checksum (simplified - in production use a proper hash)
        const checksum = await this.calculateChecksum(file);

        // Initialize file upload
        const fileInitResponse = await fetch(`/api/transfers/${transferId}/files`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          credentials: 'include',
          body: JSON.stringify({
            fileName: file.name,
            fileSize: file.size,
            mimeType: file.type || 'application/octet-stream',
            checksum: checksum
          })
        });

        if (!fileInitResponse.ok) {
          throw new Error(`Failed to initialize file upload: ${file.name}`);
        }

        const fileData = await fileInitResponse.json();
        const uploadUrl = fileData.uploadUrl;

        // Upload file to Azure Blob Storage
        const uploadResponse = await fetch(uploadUrl, {
          method: 'PUT',
          headers: {
            'x-ms-blob-type': 'BlockBlob',
            'Content-Type': file.type || 'application/octet-stream'
          },
          body: file
        });

        if (!uploadResponse.ok) {
          throw new Error(`Failed to upload file: ${file.name}`);
        }

        // Update progress
        this.uploadProgress = Math.round(((i + 1) / totalFiles) * 100);
      }

      // Step 3: Finalize transfer
      const finalizeResponse = await fetch(`/api/transfers/${transferId}/finalize`, {
        method: 'POST',
        credentials: 'include'
      });

      if (!finalizeResponse.ok) {
        throw new Error('Failed to finalize transfer');
      }

      // Success! Redirect to transfer details or success page
      alert('Transfer created successfully!');
      this.router.navigate(['/transfers', transferId]);

    } catch (error) {
      console.error('Upload error:', error);
      alert('Failed to create transfer. Please try again.');
    } finally {
      this.isUploading = false;
      this.uploadProgress = 0;
    }
  }

  // Simple checksum calculation (for demonstration)
  // In production, use a proper library like crypto-js for SHA-256
  private async calculateChecksum(file: File): Promise<string> {
    return new Promise((resolve) => {
      const reader = new FileReader();

      reader.onload = () => {
        const arrayBuffer = reader.result as ArrayBuffer;
        const bytes = new Uint8Array(arrayBuffer);

        // Simple hash - replace with proper SHA-256 in production
        let hash = 0;
        for (let i = 0; i < Math.min(bytes.length, 8192); i++) {
          hash = ((hash << 5) - hash) + bytes[i];
          hash = hash & hash;
        }

        resolve(Math.abs(hash).toString(16).padStart(16, '0'));
      };

      // Read first 8KB for checksum
      reader.readAsArrayBuffer(file.slice(0, 8192));
    });
  }
}