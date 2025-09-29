package com.constructionmanager.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0010\u000b\n\u0002\b#\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001Bw\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\u000b\u001a\u00020\n\u0012\u0014\b\u0002\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\n0\r\u0012\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\u0006\u0010\u0012\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0011\u00a2\u0006\u0002\u0010\u0014J\t\u0010&\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0003H\u00c6\u0003J\t\u0010(\u001a\u00020\u0011H\u00c6\u0003J\t\u0010)\u001a\u00020\u0003H\u00c6\u0003J\t\u0010*\u001a\u00020\u0006H\u00c6\u0003J\t\u0010+\u001a\u00020\bH\u00c6\u0003J\t\u0010,\u001a\u00020\nH\u00c6\u0003J\t\u0010-\u001a\u00020\nH\u00c6\u0003J\u0015\u0010.\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\n0\rH\u00c6\u0003J\u000f\u0010/\u001a\b\u0012\u0004\u0012\u00020\u00030\u000fH\u00c6\u0003J\t\u00100\u001a\u00020\u0011H\u00c6\u0003J\u0089\u0001\u00101\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\n2\u0014\b\u0002\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\n0\r2\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u00032\b\b\u0002\u0010\u0013\u001a\u00020\u0011H\u00c6\u0001J\u0013\u00102\u001a\u00020\u00112\b\u00103\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00104\u001a\u000205H\u00d6\u0001J\t\u00106\u001a\u00020\u0003H\u00d6\u0001R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0012\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0018R\u0011\u0010\u0013\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u001cR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0018R\u0011\u0010\u000b\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001aR\u001d\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\n0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001c\u00a8\u00067"}, d2 = {"Lcom/constructionmanager/domain/model/LaborCategory;", "", "id", "", "name", "tradeType", "Lcom/constructionmanager/domain/model/TradeType;", "skillLevel", "Lcom/constructionmanager/domain/model/SkillLevel;", "hourlyRate", "Ljava/math/BigDecimal;", "overtimeRate", "regionalRates", "", "certifications", "", "unionRate", "", "description", "isActive", "(Ljava/lang/String;Ljava/lang/String;Lcom/constructionmanager/domain/model/TradeType;Lcom/constructionmanager/domain/model/SkillLevel;Ljava/math/BigDecimal;Ljava/math/BigDecimal;Ljava/util/Map;Ljava/util/List;ZLjava/lang/String;Z)V", "getCertifications", "()Ljava/util/List;", "getDescription", "()Ljava/lang/String;", "getHourlyRate", "()Ljava/math/BigDecimal;", "getId", "()Z", "getName", "getOvertimeRate", "getRegionalRates", "()Ljava/util/Map;", "getSkillLevel", "()Lcom/constructionmanager/domain/model/SkillLevel;", "getTradeType", "()Lcom/constructionmanager/domain/model/TradeType;", "getUnionRate", "component1", "component10", "component11", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class LaborCategory {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String name = null;
    @org.jetbrains.annotations.NotNull
    private final com.constructionmanager.domain.model.TradeType tradeType = null;
    @org.jetbrains.annotations.NotNull
    private final com.constructionmanager.domain.model.SkillLevel skillLevel = null;
    @org.jetbrains.annotations.NotNull
    private final java.math.BigDecimal hourlyRate = null;
    @org.jetbrains.annotations.NotNull
    private final java.math.BigDecimal overtimeRate = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.Map<java.lang.String, java.math.BigDecimal> regionalRates = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.lang.String> certifications = null;
    private final boolean unionRate = false;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String description = null;
    private final boolean isActive = false;
    
    public LaborCategory(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.TradeType tradeType, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.SkillLevel skillLevel, @org.jetbrains.annotations.NotNull
    java.math.BigDecimal hourlyRate, @org.jetbrains.annotations.NotNull
    java.math.BigDecimal overtimeRate, @org.jetbrains.annotations.NotNull
    java.util.Map<java.lang.String, ? extends java.math.BigDecimal> regionalRates, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> certifications, boolean unionRate, @org.jetbrains.annotations.NotNull
    java.lang.String description, boolean isActive) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.TradeType getTradeType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.SkillLevel getSkillLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.math.BigDecimal getHourlyRate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.math.BigDecimal getOvertimeRate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.Map<java.lang.String, java.math.BigDecimal> getRegionalRates() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> getCertifications() {
        return null;
    }
    
    public final boolean getUnionRate() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getDescription() {
        return null;
    }
    
    public final boolean isActive() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component10() {
        return null;
    }
    
    public final boolean component11() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.TradeType component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.SkillLevel component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.math.BigDecimal component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.math.BigDecimal component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.Map<java.lang.String, java.math.BigDecimal> component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> component8() {
        return null;
    }
    
    public final boolean component9() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.LaborCategory copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.TradeType tradeType, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.SkillLevel skillLevel, @org.jetbrains.annotations.NotNull
    java.math.BigDecimal hourlyRate, @org.jetbrains.annotations.NotNull
    java.math.BigDecimal overtimeRate, @org.jetbrains.annotations.NotNull
    java.util.Map<java.lang.String, ? extends java.math.BigDecimal> regionalRates, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> certifications, boolean unionRate, @org.jetbrains.annotations.NotNull
    java.lang.String description, boolean isActive) {
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