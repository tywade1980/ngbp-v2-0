package com.constructionmanager.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b&\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0081\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t\u0012\u0006\u0010\u000b\u001a\u00020\f\u0012\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\t\u0012\u0006\u0010\u000f\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\u0006\u0010\u0013\u001a\u00020\u0014\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0016\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0018J\t\u0010,\u001a\u00020\u0003H\u00c6\u0003J\t\u0010-\u001a\u00020\u0012H\u00c6\u0003J\t\u0010.\u001a\u00020\u0014H\u00c6\u0003J\u000b\u0010/\u001a\u0004\u0018\u00010\u0016H\u00c6\u0003J\t\u00100\u001a\u00020\u0003H\u00c6\u0003J\t\u00101\u001a\u00020\u0003H\u00c6\u0003J\t\u00102\u001a\u00020\u0003H\u00c6\u0003J\t\u00103\u001a\u00020\u0003H\u00c6\u0003J\t\u00104\u001a\u00020\u0003H\u00c6\u0003J\u000f\u00105\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u00c6\u0003J\t\u00106\u001a\u00020\fH\u00c6\u0003J\u000f\u00107\u001a\b\u0012\u0004\u0012\u00020\u000e0\tH\u00c6\u0003J\t\u00108\u001a\u00020\u0010H\u00c6\u0003J\u0099\u0001\u00109\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t2\b\b\u0002\u0010\u000b\u001a\u00020\f2\u000e\b\u0002\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\t2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00142\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00162\b\b\u0002\u0010\u0017\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010:\u001a\u00020\u00122\b\u0010;\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010<\u001a\u00020=H\u00d6\u0001J\t\u0010>\u001a\u00020\u0003H\u00d6\u0001R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0013\u0010\u0015\u001a\u0004\u0018\u00010\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001cR\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001cR\u0011\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010%R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001cR\u0011\u0010\u0017\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001cR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u001cR\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u001a\u00a8\u0006?"}, d2 = {"Lcom/constructionmanager/domain/model/Worker;", "", "id", "", "firstName", "lastName", "email", "phone", "tradeTypes", "", "Lcom/constructionmanager/domain/model/TradeType;", "skillLevel", "Lcom/constructionmanager/domain/model/SkillLevel;", "certifications", "Lcom/constructionmanager/domain/model/Certification;", "hourlyRate", "Ljava/math/BigDecimal;", "isActive", "", "hireDate", "Lkotlinx/datetime/LocalDate;", "emergencyContact", "Lcom/constructionmanager/domain/model/EmergencyContact;", "notes", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Lcom/constructionmanager/domain/model/SkillLevel;Ljava/util/List;Ljava/math/BigDecimal;ZLkotlinx/datetime/LocalDate;Lcom/constructionmanager/domain/model/EmergencyContact;Ljava/lang/String;)V", "getCertifications", "()Ljava/util/List;", "getEmail", "()Ljava/lang/String;", "getEmergencyContact", "()Lcom/constructionmanager/domain/model/EmergencyContact;", "getFirstName", "getHireDate", "()Lkotlinx/datetime/LocalDate;", "getHourlyRate", "()Ljava/math/BigDecimal;", "getId", "()Z", "getLastName", "getNotes", "getPhone", "getSkillLevel", "()Lcom/constructionmanager/domain/model/SkillLevel;", "getTradeTypes", "component1", "component10", "component11", "component12", "component13", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class Worker {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String firstName = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String lastName = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String email = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String phone = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.constructionmanager.domain.model.TradeType> tradeTypes = null;
    @org.jetbrains.annotations.NotNull
    private final com.constructionmanager.domain.model.SkillLevel skillLevel = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.constructionmanager.domain.model.Certification> certifications = null;
    @org.jetbrains.annotations.NotNull
    private final java.math.BigDecimal hourlyRate = null;
    private final boolean isActive = false;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.datetime.LocalDate hireDate = null;
    @org.jetbrains.annotations.Nullable
    private final com.constructionmanager.domain.model.EmergencyContact emergencyContact = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String notes = null;
    
    public Worker(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String firstName, @org.jetbrains.annotations.NotNull
    java.lang.String lastName, @org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String phone, @org.jetbrains.annotations.NotNull
    java.util.List<? extends com.constructionmanager.domain.model.TradeType> tradeTypes, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.SkillLevel skillLevel, @org.jetbrains.annotations.NotNull
    java.util.List<com.constructionmanager.domain.model.Certification> certifications, @org.jetbrains.annotations.NotNull
    java.math.BigDecimal hourlyRate, boolean isActive, @org.jetbrains.annotations.NotNull
    kotlinx.datetime.LocalDate hireDate, @org.jetbrains.annotations.Nullable
    com.constructionmanager.domain.model.EmergencyContact emergencyContact, @org.jetbrains.annotations.NotNull
    java.lang.String notes) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFirstName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getLastName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getPhone() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.constructionmanager.domain.model.TradeType> getTradeTypes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.SkillLevel getSkillLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.constructionmanager.domain.model.Certification> getCertifications() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.math.BigDecimal getHourlyRate() {
        return null;
    }
    
    public final boolean isActive() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.datetime.LocalDate getHireDate() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.constructionmanager.domain.model.EmergencyContact getEmergencyContact() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getNotes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    public final boolean component10() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.datetime.LocalDate component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.constructionmanager.domain.model.EmergencyContact component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.constructionmanager.domain.model.TradeType> component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.SkillLevel component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.constructionmanager.domain.model.Certification> component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.math.BigDecimal component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.Worker copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String firstName, @org.jetbrains.annotations.NotNull
    java.lang.String lastName, @org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String phone, @org.jetbrains.annotations.NotNull
    java.util.List<? extends com.constructionmanager.domain.model.TradeType> tradeTypes, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.SkillLevel skillLevel, @org.jetbrains.annotations.NotNull
    java.util.List<com.constructionmanager.domain.model.Certification> certifications, @org.jetbrains.annotations.NotNull
    java.math.BigDecimal hourlyRate, boolean isActive, @org.jetbrains.annotations.NotNull
    kotlinx.datetime.LocalDate hireDate, @org.jetbrains.annotations.Nullable
    com.constructionmanager.domain.model.EmergencyContact emergencyContact, @org.jetbrains.annotations.NotNull
    java.lang.String notes) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}