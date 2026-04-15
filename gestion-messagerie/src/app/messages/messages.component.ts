import { Component, OnInit, ViewEncapsulation, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MessageService, Conversation, Message } from '../services/message.service';
import { ModalService } from '../services/modal.service';
import { take } from 'rxjs';

@Component({
  selector: 'app-messages',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './messages.component.html',
  styleUrl: './messages.component.css',
  encapsulation: ViewEncapsulation.None
})
export class MessagesComponent implements OnInit {
  conversations: Conversation[] = [];
  filteredConversations: Conversation[] = [];
  currentConversation: Conversation | undefined;
  currentMessages: Message[] = [];
  filteredMessages: Message[] = [];
  newMessageText = '';
  searchText = '';
  messageSearchText = '';
  openMenuId: number | null = null;
  openMsgMenuId: number | null = null;
  showEmojiPicker = false;
  isLoadingConversations = false;
  isLoadingMessages = false;
  editingMessage: Message | null = null;

  // Performance Metrics
  freelancerMetrics: any = null;
  conversationMetrics: any = null;
  performanceLoading = false;

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  // Common emojis like Messenger
  emojis = ['😊', '😂', '❤️', '👍', '👏', '🔥', '😍', '🎉', '😢', '😮', '🙏', '💯', '🤔', '😎', '👌', '✨', '🎊', '💪', '🙌', '😁'];

  // File attachment
  selectedFile: File | null = null;
  filePreview: { name: string; size: string; type: string } | null = null;

  // Mock current user ID for demo purposes
  myUserId = 1;

  constructor(private messageService: MessageService, private modalService: ModalService) { }

  ngOnInit(): void {
    this.loadConversations();
  }

  loadConversations(preserveSelectionId?: number) {
    this.isLoadingConversations = true;
    this.messageService.getAllConversations().subscribe({
      next: (data) => {
        this.conversations = data;
        this.filteredConversations = data;
        this.isLoadingConversations = false;

        // Restore selection if ID is provided, or select first one if nothing is selected
        if (preserveSelectionId) {
          const preserved = this.conversations.find(c => c.id === preserveSelectionId);
          if (preserved) {
            this.selectConversation(preserved);
          } else if (this.conversations.length > 0) {
            this.selectConversation(this.conversations[0]);
          }
        } else if (!this.currentConversation && this.conversations.length > 0) {
          this.selectConversation(this.conversations[0]);
        }
      },
      error: (err) => {
        console.error('Error loading conversations', err);
        this.isLoadingConversations = false;
      }
    });
  }

  selectConversation(conversation: Conversation) {
    this.currentConversation = conversation;
    this.loadMessages(conversation.id);
    this.loadPerformanceMetrics(conversation);
  }

  loadPerformanceMetrics(conversation: Conversation) {
    this.performanceLoading = true;

    // Load Freelancer PCS and related metrics
    if (conversation.freelancerId) {
      this.messageService.getFreelancerPerformance(conversation.freelancerId).subscribe({
        next: (data) => this.freelancerMetrics = data,
        error: (err) => console.error('Error loading freelancer metrics', err)
      });
    }

    // Load Conversation-specific metrics (CEI, Risk)
    this.messageService.getConversationPerformance(conversation.id).subscribe({
      next: (data) => {
        this.conversationMetrics = data;
        this.performanceLoading = false;
      },
      error: (err) => {
        console.error('Error loading conversation metrics', err);
        this.performanceLoading = false;
      }
    });
  }

  switchUser() {
    this.myUserId = this.myUserId === 1 ? 2 : 1;
    const role = this.myUserId === 1 ? 'Client' : 'Freelancer';
    alert(`Switched to user ID: ${this.myUserId} (${role})`);

    // Refresh current messages to update UI state for the new user
    if (this.currentConversation) {
      this.loadMessages(this.currentConversation.id);
      this.loadPerformanceMetrics(this.currentConversation);
    }
  }

  seedRealisticData() {
    this.messageService.seedTestData(1, 2).subscribe({
      next: (res) => {
        alert(res);
        this.loadConversations();
      },
      error: (err) => alert('Error seeding data: ' + err.message)
    });
  }

  loadMessages(conversationId: number) {
    this.isLoadingMessages = true;
    this.messageService.getMessagesByConversation(conversationId).subscribe({
      next: (data) => {
        this.currentMessages = data;
        this.filteredMessages = data;
        this.messageSearchText = ''; // Reset search when loading new conversation
        this.isLoadingMessages = false;
        this.scrollToBottom();
      },
      error: (err) => {
        console.error('Error loading messages', err);
        this.isLoadingMessages = false;
      }
    });
  }

  sendMessage() {
    if (!this.currentConversation) return;
    if (!this.newMessageText.trim() && !this.selectedFile) return;

    if (this.editingMessage) {
      this.saveEdit();
      return;
    }

    const newMessage: Message = {
      senderId: this.myUserId,
      content: this.newMessageText,
      conversationId: this.currentConversation.id
    };

    // If file is selected, convert to Base64 and add to message
    if (this.selectedFile) {
      const reader = new FileReader();
      reader.onload = () => {
        const base64 = (reader.result as string).split(',')[1]; // Remove data:image/png;base64, prefix

        newMessage.attachmentName = this.selectedFile!.name;
        newMessage.attachmentType = this.selectedFile!.type;
        newMessage.attachmentData = base64;
        newMessage.attachmentSize = this.selectedFile!.size;

        this.sendMessageToServer(newMessage);
      };
      reader.readAsDataURL(this.selectedFile);
    } else {
      this.sendMessageToServer(newMessage);
    }
  }

  private sendMessageToServer(newMessage: Message) {
    this.messageService.sendMessage(newMessage, this.currentConversation!.id).subscribe({
      next: (savedMessage) => {
        this.currentMessages.push(savedMessage);
        this.newMessageText = '';
        this.selectedFile = null;
        this.filePreview = null;
        this.scrollToBottom();
      },
      error: (err) => console.error('Error sending message', err)
    });
  }

  createNewConversation() {
    this.modalService.prompt('Enter new conversation title:', '', 'New Conversation').pipe(take(1)).subscribe(title => {
      if (title) {
        const newConv: Partial<Conversation> = {
          title: title,
          clientId: 101, // Mock
          freelancerId: 202, // Mock
          projectId: 1, // Mock
          status: 'ACTIVE'
        };
        this.messageService.createConversation(newConv).subscribe({
          next: (conv) => {
            this.conversations.push(conv);
            this.selectConversation(conv);
            this.loadConversations(conv.id);
          },
          error: (err) => console.error('Error creating conversation', err)
        });
      }
    });
  }

  deleteConversation(event: Event, id: number) {
    event.stopPropagation(); // Prevent selecting the conversation
    this.modalService.confirm('Are you sure you want to delete this conversation?', 'Delete Conversation').pipe(take(1)).subscribe(confirmed => {
      if (confirmed) {
        this.messageService.deleteConversation(id).subscribe({
          next: () => {
            this.conversations = this.conversations.filter(c => c.id !== id);
            if (this.currentConversation?.id === id) {
              this.currentConversation = undefined;
              this.currentMessages = [];
            }
            this.loadConversations();
          },
          error: (err) => console.error('Error deleting conversation', err)
        });
      }
    });
  }

  deleteMessage(id: number) {
    this.modalService.confirm('Are you sure you want to delete this message?', 'Delete Message').pipe(take(1)).subscribe(confirmed => {
      if (confirmed) {
        this.messageService.deleteMessage(id).subscribe({
          next: () => {
            this.currentMessages = this.currentMessages.filter(m => m.id !== id);
            if (this.currentConversation) {
              this.loadMessages(this.currentConversation.id);
            }
          },
          error: (err) => console.error('Error deleting message', err)
        });
      }
    });
  }

  editConversation(event: Event, conversation: Conversation) {
    event.stopPropagation();
    this.modalService.prompt('Update conversation title:', conversation.title, 'Edit Title').pipe(take(1)).subscribe(newTitle => {
      if (newTitle && newTitle !== conversation.title) {
        const updatedConv = { ...conversation, title: newTitle };
        this.messageService.updateConversation(conversation.id, updatedConv).subscribe({
          next: (savedConv) => {
            // Update in local list
            const index = this.conversations.findIndex(c => c.id === savedConv.id);
            if (index !== -1) {
              this.conversations[index] = savedConv;
            }
            if (this.currentConversation?.id === savedConv.id) {
              this.currentConversation = savedConv;
            }
            this.loadConversations(savedConv.id);
          },
          error: (err) => console.error('Error updating conversation', err)
        });
      }
    });
  }

  filterConversations() {
    if (!this.searchText.trim()) {
      this.filteredConversations = this.conversations;
    } else {
      this.filteredConversations = this.conversations.filter(c =>
        c.title.toLowerCase().includes(this.searchText.toLowerCase())
      );
    }
  }

  toggleMenu(event: Event, conversationId: number) {
    event.stopPropagation();
    this.openMenuId = this.openMenuId === conversationId ? null : conversationId;
  }

  closeMenu() {
    this.openMenuId = null;
  }

  toggleEmojiPicker(event: Event) {
    event.stopPropagation();
    this.showEmojiPicker = !this.showEmojiPicker;
  }

  insertEmoji(emoji: string) {
    this.newMessageText += emoji;
    this.showEmojiPicker = false;
  }

  closeEmojiPicker() {
    this.showEmojiPicker = false;
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      // Validate file size (5MB = 5 * 1024 * 1024 bytes)
      const maxSize = 5 * 1024 * 1024;
      if (file.size > maxSize) {
        this.modalService.alert('File size must be less than 5MB', 'File Too Large');
        input.value = '';
        return;
      }

      this.selectedFile = file;
      this.filePreview = {
        name: file.name,
        size: this.formatFileSize(file.size),
        type: file.type
      };
    }
  }

  removeFile() {
    this.selectedFile = null;
    this.filePreview = null;
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  downloadAttachment(message: Message) {
    if (!message.attachmentData || !message.attachmentName) return;

    const byteCharacters = atob(message.attachmentData);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: message.attachmentType || 'application/octet-stream' });

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = message.attachmentName;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  filterMessages() {
    if (!this.messageSearchText.trim()) {
      this.filteredMessages = this.currentMessages;
    } else {
      const searchLower = this.messageSearchText.toLowerCase();
      this.filteredMessages = this.currentMessages.filter(msg =>
        (msg.content && msg.content.toLowerCase().includes(searchLower)) ||
        (msg.attachmentName && msg.attachmentName.toLowerCase().includes(searchLower))
      );
    }
  }

  canEditMessage(message: Message): boolean {
    if (!message || message.senderId !== this.myUserId) return false;
    if (message.isRead) return false;

    if (message.sentAt) {
      const sentTime = new Date(message.sentAt).getTime();
      const now = new Date().getTime();
      const diffMinutes = (now - sentTime) / (1000 * 60);
      if (diffMinutes > 30) return false;
    }

    return true;
  }

  editMessage(message: Message) {
    if (!message.id) return;
    this.editingMessage = message;
    this.newMessageText = message.content || '';

  }

  saveEdit() {
    if (!this.editingMessage || !this.editingMessage.id) return;
    if (this.newMessageText === this.editingMessage.content) {
      this.cancelEdit();
      return;
    }

    const updatedMsg = { ...this.editingMessage, content: this.newMessageText };
    this.messageService.updateMessage(this.editingMessage.id!, updatedMsg).subscribe({
      next: (savedMsg) => {
        const index = this.currentMessages.findIndex(m => m.id === savedMsg.id);
        if (index !== -1) {
          this.currentMessages[index] = savedMsg;
        }
        this.cancelEdit();
        if (this.currentConversation) {
          this.loadMessages(this.currentConversation.id);
        }
      },
      error: (err) => {
        console.error('Error updating message', err);
        this.cancelEdit();
      }
    });
  }

  cancelEdit() {
    this.editingMessage = null;
    this.newMessageText = '';
  }

  toggleMessageMenu(event: Event, messageId: number) {
    event.stopPropagation();
    this.openMsgMenuId = this.openMsgMenuId === messageId ? null : messageId;
  }

  closeMessageMenu() {
    this.openMsgMenuId = null;
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.scrollContainer) {
        this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
      }
    }, 100);
  }
}
