package com.imaginamos.farmatodo.model.optics;

import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;

import java.io.Serializable;
import java.util.Objects;

public class ItemOptics implements Serializable {

    private Long id;

    private Boolean isMain;

    private String mainLensId;
    private LensType lensType;

    private String suggestedReplacement;

    private ShipmentType shipment;
    private String use;
    private String productPresentation;
    private String wettingTechnology;
    private String material;
    private String design;
    private String transmissibility;
    private String waterContent;

    private String eyeCondition;

    private String reverseIndicator;
    private String coreThickness;
    private String diameter;
    private String baseCurve;

    private String lensStructure;

    private String healthRegister;

    private ParametersOptics parameters;

    private ItemOptics() {
        this.id = 0L;
        this.isMain = false;
        this.mainLensId = "";
        this.suggestedReplacement = "";
        this.use = "";
        this.productPresentation = "";
        this.wettingTechnology = "";
        this.material = "";
        this.design = "";
        this.transmissibility = "";
        this.waterContent = "";
        this.eyeCondition = "";
        this.reverseIndicator = "";
        this.coreThickness = "";
        this.diameter = "";
        this.baseCurve = "";
        this.healthRegister = "";
    }

    public static ItemOptics of(ItemAlgolia itemAlgolia) {

        if (isItemAlgoliaValid(itemAlgolia))
            return new ItemOptics();

        final ItemOptics finalItemOptics = itemAlgolia.getItemOpticsComplete().getItemOptics();

        ItemOptics itemOptics = new ItemOptics();

        itemOptics.setIdNonNull(finalItemOptics.getId());
        itemOptics.setMainNonNull(finalItemOptics.getMain());
        itemOptics.setMainLensIdNonNull(finalItemOptics.getMainLensId());
        itemOptics.setSuggestedReplacementNonNull(finalItemOptics.getSuggestedReplacement());
        itemOptics.setShipmentNotNull(finalItemOptics.getShipment());
        itemOptics.setUseNonNull(finalItemOptics.getUse());
        itemOptics.setProductPresentationNonNull(finalItemOptics.getProductPresentation());
        itemOptics.setWettingTechnologyNonNull(finalItemOptics.getWettingTechnology());
        itemOptics.setMaterialNonNull(finalItemOptics.getMaterial());
        itemOptics.setDesignNonNull(finalItemOptics.getDesign());
        itemOptics.setTransmissibilityNonNull(finalItemOptics.getTransmissibility());
        itemOptics.setWaterContentNonNull(finalItemOptics.getWaterContent());
        itemOptics.setEyeConditionNonNull(finalItemOptics.getEyeCondition());
        itemOptics.setReverseIndicatorNonNull(finalItemOptics.getReverseIndicator());
        itemOptics.setCoreThicknessNonNull(finalItemOptics.getCoreThickness());
        itemOptics.setDiameterNonNull(finalItemOptics.getDiameter());
        itemOptics.setBaseCurveNonNull(finalItemOptics.getBaseCurve());
        itemOptics.setLensStructureNonNull(finalItemOptics.getLensStructure());
        itemOptics.setHealthRegisterNonNull(finalItemOptics.getHealthRegister());
        itemOptics.setParameters(buildParametersOptics(itemAlgolia));

        return itemOptics;
    }

    private static boolean isItemAlgoliaValid(ItemAlgolia itemAlgolia) {
        return Objects.isNull(itemAlgolia)
                || Objects.isNull(itemAlgolia.getItemOpticsComplete())
                || Objects.isNull(itemAlgolia.getItemOpticsComplete().getItemOptics());
    }

    public Boolean getMain() {
        return isMain;
    }

    public void setMainNonNull(Boolean main) {
        if (Objects.nonNull(main))
            isMain = main;
    }

    public LensType getLensType() {
        return lensType;
    }

    public void setLensType(LensType lensType) {
        this.lensType = lensType;
    }

    public String getMainLensId() {
        return mainLensId;
    }

    private void setMainLensIdNonNull(String mainLensId) {
        if (Objects.nonNull(mainLensId))
            this.mainLensId = mainLensId;
    }

    public String getSuggestedReplacement() {
        return suggestedReplacement;
    }

    private void setSuggestedReplacementNonNull(String suggestedReplacement) {
        if (Objects.nonNull(suggestedReplacement))
            this.suggestedReplacement = suggestedReplacement;
    }

    public ShipmentType getShipment() {
        return shipment;
    }

    public void setShipmentNotNull(ShipmentType shipment) {
        if (Objects.nonNull(shipment))
            this.shipment = shipment;
    }

    public String getUse() {
        return use;
    }

    private void setUseNonNull(String use) {
        if (Objects.nonNull(use))
            this.use = use;
    }

    public String getProductPresentation() {
        return productPresentation;
    }

    private void setProductPresentationNonNull(String productPresentation) {
        if (Objects.nonNull(productPresentation))
            this.productPresentation = productPresentation;
    }

    public String getWettingTechnology() {
        return wettingTechnology;
    }

    private void setWettingTechnologyNonNull(String wettingTechnology) {
        if (Objects.nonNull(wettingTechnology))
            this.wettingTechnology = wettingTechnology;
    }

    public String getMaterial() {
        return material;
    }

    private void setMaterialNonNull(String material) {
        if (Objects.nonNull(material))
            this.material = material;
    }

    public String getDesign() {
        return design;
    }

    private void setDesignNonNull(String design) {
        if (Objects.nonNull(design))
            this.design = design;
    }

    public String getTransmissibility() {
        return transmissibility;
    }

    private void setTransmissibilityNonNull(String transmissibility) {
        if (Objects.nonNull(transmissibility))
            this.transmissibility = transmissibility;
    }

    public String getWaterContent() {
        return waterContent;
    }

    private void setWaterContentNonNull(String waterContent) {
        if (Objects.nonNull(waterContent))
            this.waterContent = waterContent;
    }

    public String getCoreThickness() {
        return coreThickness;
    }

    private void setCoreThicknessNonNull(String coreThickness) {
        if (Objects.nonNull(coreThickness))
            this.coreThickness = coreThickness;
    }

    public String getDiameter() {
        return diameter;
    }

    private void setDiameterNonNull(String diameter) {
        if (Objects.nonNull(diameter))
            this.diameter = diameter;
    }

    public String getBaseCurve() {
        return baseCurve;
    }

    private void setBaseCurveNonNull(String baseCurve) {
        if (Objects.nonNull(baseCurve))
            this.baseCurve = baseCurve;
    }

    public String getLensStructure() {
        return lensStructure;
    }

    private void setLensStructureNonNull(String lensStructure) {
        if (Objects.nonNull(lensStructure))
            this.lensStructure = lensStructure;
    }

    public String getEyeCondition() {
        return eyeCondition;
    }

    private void setEyeConditionNonNull(String eyeCondition) {
        if (Objects.nonNull(eyeCondition))
            this.eyeCondition = eyeCondition;
    }

    public String getHealthRegister() {
        return healthRegister;
    }

    private void setHealthRegisterNonNull(String healthRegister) {
        if (Objects.nonNull(healthRegister))
            this.healthRegister = healthRegister;
    }

    public Long getId() {
        return id;
    }

    private void setIdNonNull(Long id) {
        if (Objects.nonNull(id))
            this.id = id;
    }

    public String getReverseIndicator() {
        return reverseIndicator;
    }

    private void setReverseIndicatorNonNull(String reverseIndicator) {
        if (Objects.nonNull(reverseIndicator))
            this.reverseIndicator = reverseIndicator;
    }

    public ParametersOptics getParameters() {
        return parameters;
    }

    public void setParameters(ParametersOptics parameters) {
        this.parameters = parameters;
    }

    private static ParametersOptics buildParametersOptics(ItemAlgolia itemAlgolia) {
        if (isParametersInvalid(itemAlgolia))
            return new ParametersOptics();

        ParametersOptics parametersOptics = new ParametersOptics();
        parametersOptics.setPowerNonNull(itemAlgolia.getItemOpticsComplete().getItemOptics().getParameters().getPower());
        parametersOptics.setCylinderNonNull(itemAlgolia.getItemOpticsComplete().getItemOptics().getParameters().getCylinder());
        parametersOptics.setAxleNonNull(itemAlgolia.getItemOpticsComplete().getItemOptics().getParameters().getAxle());
        parametersOptics.setAddictionNonNull(itemAlgolia.getItemOpticsComplete().getItemOptics().getParameters().getAddiction());
        parametersOptics.setLensColorNonNull(itemAlgolia.getItemOpticsComplete().getItemOptics().getParameters().getLensColor());
        return parametersOptics;
    }

    private static boolean isParametersInvalid(ItemAlgolia itemAlgolia) {
        return Objects.isNull(itemAlgolia.getItemOpticsComplete().getItemOptics().getParameters());
    }

    @Override
    public String toString() {
        return "ItemOptics{" +
                "id=" + id +
                ", isMain=" + isMain +
                ", mainLensId=" + mainLensId +
                ", lensType=" + lensType +
                ", suggestedReplacement='" + suggestedReplacement + '\'' +
                ", shipment=" + shipment +
                ", use='" + use + '\'' +
                ", productPresentation='" + productPresentation + '\'' +
                ", wettingTechnology='" + wettingTechnology + '\'' +
                ", material='" + material + '\'' +
                ", design='" + design + '\'' +
                ", transmissibility='" + transmissibility + '\'' +
                ", waterContent='" + waterContent + '\'' +
                ", eyeCondition='" + eyeCondition + '\'' +
                ", reverseIndicator='" + reverseIndicator + '\'' +
                ", coreThickness='" + coreThickness + '\'' +
                ", diameter='" + diameter + '\'' +
                ", baseCurve='" + baseCurve + '\'' +
                ", lensStructure='" + lensStructure + '\'' +
                ", healthRegister='" + healthRegister + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
