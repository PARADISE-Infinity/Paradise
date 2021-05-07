/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration;

import de.dlr.premise.migration.strategies.MigrateUnversionedToV102;
import de.dlr.premise.migration.strategies.MigrateV102ToV103;
import de.dlr.premise.migration.strategies.MigrateV103ToV104;
import de.dlr.premise.migration.strategies.MigrateV104ToV105;
import de.dlr.premise.migration.strategies.MigrateV105ToV106;
import de.dlr.premise.migration.strategies.MigrateV106ToV107;
import de.dlr.premise.migration.strategies.MigrateV107ToV108;
import de.dlr.premise.migration.strategies.MigrateV108ToV109;
import de.dlr.premise.migration.strategies.MigrateV109ToV110;
import de.dlr.premise.migration.strategies.MigrateV110ToV111;
import de.dlr.premise.migration.strategies.MigrateV111ToV112;
import de.dlr.premise.migration.strategies.MigrateV112ToV113;
import de.dlr.premise.migration.strategies.MigrateV113ToV114;
import de.dlr.premise.migration.strategies.MigrateV114ToV115;
import de.dlr.premise.migration.strategies.MigrateV115ToV116;
import de.dlr.premise.migration.strategies.MigrateV116ToV117;
import de.dlr.premise.migration.strategies.MigrateV117ToV118;
import de.dlr.premise.migration.strategies.MigrateV118ToV119;
import de.dlr.premise.migration.strategies.MigrateV119ToV120;
import de.dlr.premise.migration.strategies.MigrateV120ToV121;
import de.dlr.premise.migration.strategies.MigrateV121ToV122;
import de.dlr.premise.migration.strategies.MigrateV122ToV123;
import de.dlr.premise.migration.strategies.MigrateV123ToV124;
import de.dlr.premise.migration.strategies.MigrateV124ToV125;
import de.dlr.premise.migration.strategies.MigrateV125ToV126;
import de.dlr.premise.migration.strategies.MigrateV126ToV127;
import de.dlr.premise.migration.strategies.MigrateV127ToV128;
import de.dlr.premise.migration.strategies.MigrateV128ToV129;
import de.dlr.premise.migration.strategies.MigrateV129ToV130;
import de.dlr.premise.migration.strategies.MigrateV130ToV131;
import de.dlr.premise.migration.strategies.MigrateV131ToV132;
import de.dlr.premise.migration.strategies.MigrateV132ToV133;

public class MigrationStrategy {

    /**
     * @param model
     * @return
     */
    public static IPremiseMigration getInstance(final MigrationModel model) {
        return getFileBasedInstance(model);
    }

    /**
     * Migrates a model into the target model Version
     * 
     * @param model
     * @param targetVersion
     */
    public static void migrate(MigrationModel model, final String targetVersion) throws MigrationMissingException {

        while (!targetVersion.equals(model.getVersion())) {
            IPremiseMigration inter = MigrationStrategy.getInstance(model);
            if (inter != null) {
                inter.migrate(model);
                model.setVersion(inter.getTargetVersion());
            } else {
                throw new MigrationMissingException(model.getVersion());
            }
        }
    }

    private static IPremiseMigration getFileBasedInstance(MigrationModel model) {

        ModelFileType type = model.getModelFileType();
        String modelVersion = model.getVersion();

        if (modelVersion == null) {
            switch (type) {
                case SYSTEM:
                case FUNCTION:
                    return new MigrateUnversionedToV102();
                case REPRESENTATION:
                case REGISTRY:
                case FUNCTIONPOOL:
                    return new MigrateV113ToV114();
                case COMPONENT:
                    return new MigrateV119ToV120();
                case UNKNOWN:
                    return null;
            }
        }
        
        if (modelVersion.equals(ModelVersion.V102.toString())) {
            return new MigrateV102ToV103();
        }
        if (modelVersion.equals(ModelVersion.V103.toString())) {
            return new MigrateV103ToV104();
        }
        if (modelVersion.equals(ModelVersion.V104.toString())) {
            return new MigrateV104ToV105();
        }
        if (modelVersion.equals(ModelVersion.V105.toString())) {
            return new MigrateV105ToV106();
        }
        if (modelVersion.equals(ModelVersion.V106.toString())) {
            return new MigrateV106ToV107();
        }

        if (modelVersion.equals(ModelVersion.V107.toString())) {
            return new MigrateV107ToV108();
        }

        if (modelVersion.equals(ModelVersion.V108.toString())) {
            return new MigrateV108ToV109();
        }

        if (modelVersion.equals(ModelVersion.V109.toString())) {
            return new MigrateV109ToV110();
        }

        if (modelVersion.equals(ModelVersion.V110.toString())) {
            return new MigrateV110ToV111();
        }

        if (modelVersion.equals(ModelVersion.V111.toString())) {
            return new MigrateV111ToV112();
        }

        if (modelVersion.equals(ModelVersion.V112.toString())) {
            return new MigrateV112ToV113();
        }

        if (modelVersion.equals(ModelVersion.V113.toString())) {
            return new MigrateV113ToV114();
        }

        if (modelVersion.equals(ModelVersion.V114.toString())) {
            return new MigrateV114ToV115();
        }

        if (modelVersion.equals(ModelVersion.V115.toString())) {
            return new MigrateV115ToV116();
        }
        
        if (modelVersion.equals(ModelVersion.V116.toString())) {
            return new MigrateV116ToV117();
        }
        
        if (modelVersion.equals(ModelVersion.V117.toString())) {
            return new MigrateV117ToV118();
        }
        
        if (modelVersion.equals(ModelVersion.V118.toString())) {
            return new MigrateV118ToV119();
        }
        
        if (modelVersion.equals(ModelVersion.V119.toString())) {
            return new MigrateV119ToV120();
        }

        if (modelVersion.equals(ModelVersion.V120.toString())) {
            return new MigrateV120ToV121();
        }        
        
        if (modelVersion.equals(ModelVersion.V121.toString())) {
            return new MigrateV121ToV122();
        }
        
        if (modelVersion.equals(ModelVersion.V122.toString())) {
            return new MigrateV122ToV123();
        }
        
        if (modelVersion.equals(ModelVersion.V123.toString())) {
            return new MigrateV123ToV124();
        }
        
        if (modelVersion.equals(ModelVersion.V124.toString())) {
            return new MigrateV124ToV125();
        }        
        
        if (modelVersion.equals(ModelVersion.V125.toString())) {
            return new MigrateV125ToV126();
        }
        
        if (modelVersion.equals(ModelVersion.V126.toString())) {
            return new MigrateV126ToV127();
        }
        
        if (modelVersion.equals(ModelVersion.V127.toString())) {
            return new MigrateV127ToV128();
        }
        
        if (modelVersion.equals(ModelVersion.V128.toString())) {
            return new MigrateV128ToV129();
        }
        
        if (modelVersion.equals(ModelVersion.V129.toString())) {
            return new MigrateV129ToV130();
        }

        if (modelVersion.equals(ModelVersion.V130.toString())) {
            return new MigrateV130ToV131();
        }

        if (modelVersion.equals(ModelVersion.V131.toString())) {
            return new MigrateV131ToV132();
        }

        if (modelVersion.equals(ModelVersion.V132.toString())) {
            return new MigrateV132ToV133();
        }
        
        return null;
    }
}
