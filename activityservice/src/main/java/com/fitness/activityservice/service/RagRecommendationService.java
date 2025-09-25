package com.fitness.activityservice.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
@Service
public class RagRecommendationService {
    interface WellnessAssistant{
        String chat(String userMessage);
    }
    private WellnessAssistant assistant;
    @PostConstruct
    public void initialize(){
        Path knowledgeBasePath = getPathFromResource("knowledge-base");
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(knowledgeBasePath,new TextDocumentParser());
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300,0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();
        ChatLanguageModel model  = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .build();
        assistant = AiServices.builder(WellnessAssistant.class)
                .chatLanguageModel(model)
                .contentRetriever(contentRetriever)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
    public String getRecommendation(String activity){
        if(assistant == null){
            return "AI Assistant not ready";
        }
        String prompt = String.format("The user's current activity is '%s'."+"Based on the provided context, give one short, encouraging, and actionable wellness tip"+"Do not give medical advice",activity);
        return assistant.chat(prompt);
    }
    private Path getPathFromResource(String path){
        try{
            URL resourceUrl = getClass().getClassLoader().getResource(path);
            if(resourceUrl == null){
                throw new RuntimeException("Resource not found"+path);
            }
            return Paths.get(resourceUrl.toURI());
        }
        catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
}
