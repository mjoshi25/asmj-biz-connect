package com.joshi.twitterclone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {
    private String groupName;
    @Builder.Default
    private List<String> memberUsernames = new ArrayList<>();

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getMemberUsernames() {
        return this.memberUsernames;
    }

    public void setMemberUsernames(List<String> memberUsernames) {
        this.memberUsernames = memberUsernames;
    }
}