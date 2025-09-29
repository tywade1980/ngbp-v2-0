package com.constructionmanager.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b&\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B}\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\t\u0012\u0006\u0010\u000b\u001a\u00020\f\u0012\u0006\u0010\r\u001a\u00020\f\u0012\u0006\u0010\u000e\u001a\u00020\u0003\u0012\u0006\u0010\u000f\u001a\u00020\u0010\u0012\u0006\u0010\u0011\u001a\u00020\u0010\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0010\u0012\u0006\u0010\u0013\u001a\u00020\u0014\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0016J\t\u0010+\u001a\u00020\u0003H\u00c6\u0003J\t\u0010,\u001a\u00020\u0010H\u00c6\u0003J\t\u0010-\u001a\u00020\u0010H\u00c6\u0003J\u000b\u0010.\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\t\u0010/\u001a\u00020\u0014H\u00c6\u0003J\t\u00100\u001a\u00020\u0003H\u00c6\u0003J\t\u00101\u001a\u00020\u0003H\u00c6\u0003J\t\u00102\u001a\u00020\u0003H\u00c6\u0003J\t\u00103\u001a\u00020\u0007H\u00c6\u0003J\t\u00104\u001a\u00020\tH\u00c6\u0003J\t\u00105\u001a\u00020\tH\u00c6\u0003J\t\u00106\u001a\u00020\fH\u00c6\u0003J\t\u00107\u001a\u00020\fH\u00c6\u0003J\t\u00108\u001a\u00020\u0003H\u00c6\u0003J\u0097\u0001\u00109\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\f2\b\b\u0002\u0010\u000e\u001a\u00020\u00032\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00102\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00102\b\b\u0002\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010:\u001a\u00020;2\b\u0010<\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010=\u001a\u00020>H\u00d6\u0001J\t\u0010?\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0011\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0018R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001bR\u0011\u0010\u0015\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001bR\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0018R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001bR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010\n\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010#R\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010&R\u0011\u0010\u000e\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001bR\u0011\u0010\r\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010)R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010)\u00a8\u0006@"}, d2 = {"Lcom/constructionmanager/domain/model/MaterialOrder;", "", "id", "", "projectId", "materialId", "material", "Lcom/constructionmanager/domain/model/Material;", "quantityOrdered", "", "quantityReceived", "unitPrice", "Ljava/math/BigDecimal;", "totalCost", "supplier", "orderDate", "Lkotlinx/datetime/LocalDate;", "expectedDeliveryDate", "actualDeliveryDate", "status", "Lcom/constructionmanager/domain/model/OrderStatus;", "notes", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/constructionmanager/domain/model/Material;DDLjava/math/BigDecimal;Ljava/math/BigDecimal;Ljava/lang/String;Lkotlinx/datetime/LocalDate;Lkotlinx/datetime/LocalDate;Lkotlinx/datetime/LocalDate;Lcom/constructionmanager/domain/model/OrderStatus;Ljava/lang/String;)V", "getActualDeliveryDate", "()Lkotlinx/datetime/LocalDate;", "getExpectedDeliveryDate", "getId", "()Ljava/lang/String;", "getMaterial", "()Lcom/constructionmanager/domain/model/Material;", "getMaterialId", "getNotes", "getOrderDate", "getProjectId", "getQuantityOrdered", "()D", "getQuantityReceived", "getStatus", "()Lcom/constructionmanager/domain/model/OrderStatus;", "getSupplier", "getTotalCost", "()Ljava/math/BigDecimal;", "getUnitPrice", "component1", "component10", "component11", "component12", "component13", "component14", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class MaterialOrder {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String projectId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String materialId = null;
    @org.jetbrains.annotations.NotNull()
    private final com.constructionmanager.domain.model.Material material = null;
    private final double quantityOrdered = 0.0;
    private final double quantityReceived = 0.0;
    @org.jetbrains.annotations.NotNull()
    private final java.math.BigDecimal unitPrice = null;
    @org.jetbrains.annotations.NotNull()
    private final java.math.BigDecimal totalCost = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String supplier = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.datetime.LocalDate orderDate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.datetime.LocalDate expectedDeliveryDate = null;
    @org.jetbrains.annotations.Nullable()
    private final kotlinx.datetime.LocalDate actualDeliveryDate = null;
    @org.jetbrains.annotations.NotNull()
    private final com.constructionmanager.domain.model.OrderStatus status = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String notes = null;
    
    public MaterialOrder(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String projectId, @org.jetbrains.annotations.NotNull()
    java.lang.String materialId, @org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Material material, double quantityOrdered, double quantityReceived, @org.jetbrains.annotations.NotNull()
    java.math.BigDecimal unitPrice, @org.jetbrains.annotations.NotNull()
    java.math.BigDecimal totalCost, @org.jetbrains.annotations.NotNull()
    java.lang.String supplier, @org.jetbrains.annotations.NotNull()
    kotlinx.datetime.LocalDate orderDate, @org.jetbrains.annotations.NotNull()
    kotlinx.datetime.LocalDate expectedDeliveryDate, @org.jetbrains.annotations.Nullable()
    kotlinx.datetime.LocalDate actualDeliveryDate, @org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.OrderStatus status, @org.jetbrains.annotations.NotNull()
    java.lang.String notes) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getProjectId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMaterialId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.Material getMaterial() {
        return null;
    }
    
    public final double getQuantityOrdered() {
        return 0.0;
    }
    
    public final double getQuantityReceived() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.math.BigDecimal getUnitPrice() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.math.BigDecimal getTotalCost() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSupplier() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.datetime.LocalDate getOrderDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.datetime.LocalDate getExpectedDeliveryDate() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlinx.datetime.LocalDate getActualDeliveryDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.OrderStatus getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getNotes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.datetime.LocalDate component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.datetime.LocalDate component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlinx.datetime.LocalDate component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.OrderStatus component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.Material component4() {
        return null;
    }
    
    public final double component5() {
        return 0.0;
    }
    
    public final double component6() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.math.BigDecimal component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.math.BigDecimal component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.MaterialOrder copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String projectId, @org.jetbrains.annotations.NotNull()
    java.lang.String materialId, @org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Material material, double quantityOrdered, double quantityReceived, @org.jetbrains.annotations.NotNull()
    java.math.BigDecimal unitPrice, @org.jetbrains.annotations.NotNull()
    java.math.BigDecimal totalCost, @org.jetbrains.annotations.NotNull()
    java.lang.String supplier, @org.jetbrains.annotations.NotNull()
    kotlinx.datetime.LocalDate orderDate, @org.jetbrains.annotations.NotNull()
    kotlinx.datetime.LocalDate expectedDeliveryDate, @org.jetbrains.annotations.Nullable()
    kotlinx.datetime.LocalDate actualDeliveryDate, @org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.OrderStatus status, @org.jetbrains.annotations.NotNull()
    java.lang.String notes) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}