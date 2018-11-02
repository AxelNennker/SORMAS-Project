package de.symeda.sormas.app.backend.facility;

import android.util.Log;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.app.backend.common.AbstractAdoDao;
import de.symeda.sormas.app.backend.common.AdoDtoHelper;
import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.region.Community;
import de.symeda.sormas.app.backend.region.District;
import de.symeda.sormas.app.backend.region.Region;
import de.symeda.sormas.app.rest.RetroProvider;
import de.symeda.sormas.app.rest.ServerCommunicationException;
import de.symeda.sormas.app.rest.ServerConnectionException;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Martin Wahnschaffe on 27.07.2016.
 */
public class FacilityDtoHelper extends AdoDtoHelper<Facility, FacilityDto> {

    @Override
    protected Class<Facility> getAdoClass() {
        return Facility.class;
    }

    @Override
    protected Class<FacilityDto> getDtoClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Call<List<FacilityDto>> pullAllSince(long since) {
        throw new UnsupportedOperationException("Use pullAllByRegionSince");
    }

    @Override
    protected Call<List<FacilityDto>> pullByUuids(List<String> uuids) {
        return RetroProvider.getFacilityFacade().pullByUuids(uuids);
    }

    protected Call<List<FacilityDto>> pullAllByRegionSince(Region region, long since) {
        return RetroProvider.getFacilityFacade().pullAllByRegionSince(region.getUuid(), since);
    }

    protected Call<List<FacilityDto>> pullAllWithoutRegionSince(long since) {
        return RetroProvider.getFacilityFacade().pullAllWithoutRegionSince(since);
    }

    @Override
    protected Call<Integer> pushAll(List<FacilityDto> facilityDtos) {
        throw new UnsupportedOperationException("Entity is infrastructure");
    }

    /**
     * Pulls the data chunkwise per region
     *
     * @param markAsRead
     * @throws DaoException
     * @throws SQLException
     * @throws IOException
     */
    @Override
    public void pullEntities(final boolean markAsRead) throws DaoException, ServerCommunicationException, ServerConnectionException {
        try {
            final FacilityDao facilityDao = DatabaseHelper.getFacilityDao();

            List<Region> regions = DatabaseHelper.getRegionDao().queryForAll();
            for (Region region : regions) {
                Date maxModifiedDate = facilityDao.getLatestChangeDateByRegion(region);
                long maxModifiedTime = maxModifiedDate != null ? maxModifiedDate.getTime() + 1 : 0;
                databaseWasEmpty = maxModifiedDate == null;

                Call<List<FacilityDto>> dtoCall = pullAllByRegionSince(region, maxModifiedTime);
                if (dtoCall == null) {
                    return;
                }
                handlePullResponse(markAsRead, facilityDao, dtoCall.execute());
            }

            {
                // Pull 'Other' health facility which has no region set
                Date maxModifiedDate = facilityDao.getLatestChangeDateByRegion(null);
                long maxModifiedTime = maxModifiedDate != null ? maxModifiedDate.getTime() + 1 : 0;
                databaseWasEmpty = maxModifiedDate == null;

                Call<List<FacilityDto>> dtoCall = pullAllWithoutRegionSince(maxModifiedTime);
                if (dtoCall == null) {
                    return;
                }
                handlePullResponse(markAsRead, facilityDao, dtoCall.execute());
            }

        } catch (IOException e) {
            throw new ServerCommunicationException(e);
        } finally {
            databaseWasEmpty = false;
        }
    }

    // performance tweak: only query for existing during pull, when database was not empty
    private boolean databaseWasEmpty = false;

    /**
     * Overriden for performance reasons. No merge needed when database was empty.
     */
    @Override
    protected int handlePullResponse(final boolean markAsRead, final AbstractAdoDao<Facility> dao, Response<List<FacilityDto>> response) throws ServerCommunicationException, DaoException, ServerConnectionException {
        if (!response.isSuccessful()) {
            RetroProvider.throwException(response);
        }

        final FacilityDao facilityDao = (FacilityDao) dao;

        final List<FacilityDto> result = response.body();
        if (result != null && result.size() > 0) {
            preparePulledResult(result);
            facilityDao.callBatchTasks(new Callable<Void>() {
                public Void call() throws Exception {

                    for (FacilityDto dto : result) {
                        Facility existing = null;
                        if (!databaseWasEmpty) {
                            existing = facilityDao.queryUuid(dto.getUuid());
                        }
                        Facility existingOrNew = fillOrCreateFromDto(existing, dto);
                        if (markAsRead) {
                            existingOrNew.setLastOpenedDate(DateHelper.addSeconds(new Date(), 5));
                        }
                        facilityDao.updateOrCreate(existingOrNew);
                    }
                    return null;
                }
            });

            Log.d(dao.getTableName(), "Pulled: " + result.size());
            return result.size();
        }

        return 0;
    }

    // cache of last queried entities
    private Community lastCommunity = null;
    private District lastDistrict = null;
    private Region lastRegion = null;

    @Override
    public void fillInnerFromDto(Facility target, FacilityDto source) {

        target.setName(source.getName());

        // keep a cache to improve performance
        if (source.getCommunity() != null) {
            if (lastCommunity == null || !lastCommunity.getUuid().equals(source.getCommunity().getUuid())) {
                lastCommunity = DatabaseHelper.getCommunityDao().getByReferenceDto(source.getCommunity());
            }
        } else {
            lastCommunity = null;
        }
        target.setCommunity(lastCommunity);
        if (source.getDistrict() != null) {
            if (lastDistrict == null || !lastDistrict.getUuid().equals(source.getDistrict().getUuid())) {
                lastDistrict = DatabaseHelper.getDistrictDao().getByReferenceDto(source.getDistrict());
            }
        } else {
            lastDistrict = null;
        }
        target.setDistrict(lastDistrict);
        if (source.getRegion() != null) {
            if (lastRegion == null || !lastRegion.getUuid().equals(source.getRegion().getUuid())) {
                lastRegion = DatabaseHelper.getRegionDao().getByReferenceDto(source.getRegion());
            }
        } else {
            lastRegion = null;
        }
        target.setRegion(lastRegion);

        target.setCity(source.getCity());
        target.setLatitude(source.getLatitude());
        target.setLongitude(source.getLongitude());
        target.setPublicOwnership(source.isPublicOwnership());
        target.setType(source.getType());
    }

    @Override
    public void fillInnerFromAdo(FacilityDto facilityDto, Facility facility) {
        throw new UnsupportedOperationException();
    }

    public static FacilityReferenceDto toReferenceDto(Facility ado) {
        if (ado == null) {
            return null;
        }
        FacilityReferenceDto dto = new FacilityReferenceDto(ado.getUuid());

        return dto;
    }
}
