package com.sp26se041.edubridgehcm.responses;

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
public class StorageTreeNode {
    private String name;
    private String path;
    private String type; // folder | file
    private String publicUrl; // chỉ có với file
    private List<StorageTreeNode> children;
}
