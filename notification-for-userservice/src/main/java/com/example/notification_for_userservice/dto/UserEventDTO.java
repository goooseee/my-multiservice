package com.example.notification_for_userservice.dto;


public record UserEventDTO(
	    String email,
	    EventType eventType
	) {
	    public enum EventType {
	        CREATED,
	        DELETED
	    }
	}