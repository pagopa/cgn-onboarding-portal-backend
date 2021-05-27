package it.gov.pagopa.cgn.portal.load_performance;

import it.gov.pagopa.cgn.portal.CGNOnboardingPortal;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.*;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.BackofficeAgreementService;
import it.gov.pagopa.cgn.portal.service.DiscountService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Slf4j
@SpringBootApplication
public class SampleDataGenerate implements CommandLineRunner {

    private static ConfigurableApplicationContext applicationContext;

    private static final List<String> profileNameList = Arrays.asList(
            "ferrari", "feltrinelli", "esselunga", "alitalia", "carrefour", "eni", "lamborghini");

    @Autowired
    private AgreementRepository agreementRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    protected ProfileRepository profileRepository;

    @Autowired
    protected AgreementUserRepository userRepository;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private BackofficeAgreementService backofficeAgreementService;


    public static void main(String[] args) {
        applicationContext = SpringApplication.run(CGNOnboardingPortal.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("args: " + Arrays.toString(args));
        int numTimes = 10000;
        if (args.length > 0) {
            numTimes = Integer.parseInt(args[0]);
        }
        //clean();
        createApprovedAgreementStressTest(numTimes);
        log.info("Completed");
        System.exit(0);
    }

    private void clean() {
        documentRepository.deleteAll();
        discountRepository.deleteAll();
        profileRepository.deleteAll();
        agreementRepository.deleteAll();
        userRepository.deleteAll();
    }


    protected void createApprovedAgreementStressTest(int numberToCreate) {
        TestUtils.setAdminAuth();
        IntStream.range(0, numberToCreate).forEach(idx -> {
            AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(Instant.now().getEpochSecond() + "" + idx);

            ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(
                    agreementEntity, getRandomSaleChannelEnum(), getRandomDiscountCodeTypeEnum());
            if (!profileEntity.getSalesChannel().equals(SalesChannelEnum.ONLINE)) {
                createSampleAddress(profileEntity);
            }
            profileEntity.setFullName(getRandomProfileName() + "_" + idx);
            profileEntity = profileService.createProfile(profileEntity, agreementEntity.getId());

            //creating discount
            List<DiscountEntity> discountList = new ArrayList<>();
            AgreementEntity finalAgreementEntity = agreementEntity;
            IntStream.of(new Random().nextInt(5)).forEach(discountIdx -> {
                DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithoutProduct(finalAgreementEntity);
                discountEntity.setProducts(getRandomProducts(discountEntity));
                discountEntity.setName(discountEntity.getName() + "_" + idx + "_" + discountIdx);
                discountEntity = discountService.createDiscount(finalAgreementEntity.getId(), discountEntity);
                discountList.add(discountEntity);
            });


            List<DocumentEntity> documentEntityList = saveSampleDocuments(agreementEntity);
            agreementEntity = agreementService.requestApproval(agreementEntity.getId());
            agreementEntity.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
            agreementEntity = agreementRepository.save(agreementEntity);
            documentEntityList.addAll(saveBackofficeSampleDocuments(agreementEntity));
            agreementEntity = backofficeAgreementService.approveAgreement(agreementEntity.getId());
            discountList.forEach(d -> discountService.publishDiscount(finalAgreementEntity.getId(), d.getId()));
        });

    }

    protected List<DocumentEntity> saveSampleDocuments(AgreementEntity agreementEntity) {
        List<DocumentEntity> documentList = TestUtils.createSampleDocumentList(agreementEntity);
        return documentRepository.saveAll(documentList);
    }

    protected List<DocumentEntity> saveBackofficeSampleDocuments(AgreementEntity agreementEntity) {
        List<DocumentEntity> documentList = TestUtils.createSampleBackofficeDocumentList(agreementEntity);
        return documentRepository.saveAll(documentList);
    }

    private SalesChannelEnum getRandomSaleChannelEnum() {
        int randomVal = new Random().nextInt(SalesChannelEnum.values().length);
        return SalesChannelEnum.values()[randomVal];

    }

    private DiscountCodeTypeEnum getRandomDiscountCodeTypeEnum() {
        return new Random().nextBoolean() ? DiscountCodeTypeEnum.API : DiscountCodeTypeEnum.STATIC;
    }

    public static void createSampleAddress(ProfileEntity profileEntity) {
        final double minLatitude = 44.560730;
        final double maxLatitude = 45.809394;
        final double minLongitude = 6.869164;
        final double maxLongitude = 12.052098;
        List<AddressEntity> list = new ArrayList<>();
        IntStream.of(new Random().nextInt(10)).forEach(idx -> {
            AddressEntity addressEntity = new AddressEntity();
            addressEntity.setProfile(profileEntity);
            addressEntity.setFullAddress("SAMPLE ADDRESS");
            addressEntity.setLatitude(getRandomDoubleNumber(minLatitude, maxLatitude));
            addressEntity.setLongitude(getRandomDoubleNumber(minLongitude, maxLongitude));
            list.add(addressEntity);
        });
        profileEntity.addAddressList(list);
    }

    public static double getRandomDoubleNumber(double minimum, double maximum) {
        return new Random().nextDouble() * (maximum - minimum) + minimum;
    }

    public List<DiscountProductEntity> getRandomProducts(DiscountEntity discountEntity) {
        List<DiscountProductEntity> productEntityList = new ArrayList<>(3);
        IntStream.of(new Random().nextInt(3)).forEach(idx -> {
            DiscountProductEntity productEntity = new DiscountProductEntity();
            productEntity.setProductCategory(getRandomProductCategoryEnum());
            productEntity.setDiscount(discountEntity);
            productEntityList.add(productEntity);
        });
        return productEntityList;
    }

    private ProductCategoryEnum getRandomProductCategoryEnum() {
        int randomVal = new Random().nextInt(ProductCategoryEnum.values().length);
        return ProductCategoryEnum.values()[randomVal];
    }

    private String getRandomProfileName() {
        int randomVal = new Random().nextInt(profileNameList.size());
        return profileNameList.get(randomVal);
    }

}