package com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PlayerList extends AuditableAbstractAggregateRoot<PlayerList> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Rooms room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    public PlayerList() {}

    public PlayerList(Rooms room, User user) {
        this.room = room;
        this.user = user;
    }

    public Long getRoomId() {
        return room != null ? room.getId() : null;
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public Long getChatRoomId() {
        return chatRoom != null ? chatRoom.getId() : null;
    }
}