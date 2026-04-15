import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, retry, delay } from 'rxjs';

export interface Message {
    id?: number;
    senderId: number;
    content: string;
    type?: string;
    sentAt?: string;
    isRead?: boolean;
    isDeleted?: boolean;
    isEdited?: boolean;
    conversationId?: number; // For sending

    // File attachment fields
    attachmentName?: string;
    attachmentType?: string;
    attachmentData?: string;
    attachmentSize?: number;
}

export interface Conversation {
    id: number;
    projectId?: number;
    clientId?: number;
    freelancerId?: number;
    status?: string;
    title: string;
    lastMessageAt?: string;

    avatar?: string;
    role?: string;
    preview?: string;
    time?: string;
    badge?: number;
}

@Injectable({
    providedIn: 'root'
})
export class MessageService {
    // Pointing to API Gateway
    private apiUrl = 'http://localhost:8089/api';

    constructor(private http: HttpClient) { }

    // Conversations
    getAllConversations(): Observable<Conversation[]> {
        return this.http.get<Conversation[]>(`${this.apiUrl}/conversations`).pipe(
            // If Gateway returns error (e.g. 404/503/504) during startup, 
            // retry 10 times with a 2-second delay between each
            retry({ count: 3, delay: 2000 })
        );
    }

    getConversationById(id: number): Observable<Conversation> {
        return this.http.get<Conversation>(`${this.apiUrl}/conversations/${id}`);
    }

    // Messages
    getAllMessages(): Observable<Message[]> {
        return this.http.get<Message[]>(`${this.apiUrl}/messages`);
    }

    getMessagesByConversation(conversationId: number): Observable<Message[]> {
        return this.http.get<Message[]>(`${this.apiUrl}/messages/conversation/${conversationId}`);
    }

    sendMessage(message: Message, conversationId: number): Observable<Message> {
        const payload = { ...message, conversationId };
        return this.http.post<Message>(`${this.apiUrl}/messages`, payload);
    }

    // CRUD Operations
    createConversation(conversation: Partial<Conversation>): Observable<Conversation> {
        return this.http.post<Conversation>(`${this.apiUrl}/conversations`, conversation);
    }

    updateConversation(id: number, conversation: Partial<Conversation>): Observable<Conversation> {
        return this.http.put<Conversation>(`${this.apiUrl}/conversations/${id}`, conversation);
    }

    deleteConversation(id: number): Observable<string> {
        return this.http.delete(`${this.apiUrl}/conversations/${id}`, { responseType: 'text' });
    }

    updateMessage(id: number, message: Partial<Message>): Observable<Message> {
        return this.http.put<Message>(`${this.apiUrl}/messages/${id}`, message);
    }

    deleteMessage(id: number): Observable<string> {
        return this.http.delete(`${this.apiUrl}/messages/${id}`, { responseType: 'text' });
    }

    // Performance Metrics
    getFreelancerPerformance(id: number): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/performance/freelancer/${id}`);
    }

    getConversationPerformance(id: number): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/performance/conversation/${id}`);
    }

    seedTestData(clientId: number = 1, freelancerId: number = 2): Observable<string> {
        return this.http.post(`${this.apiUrl}/performance/seed-test-data?clientId=${clientId}&freelancerId=${freelancerId}`, {}, { responseType: 'text' });
    }
}
