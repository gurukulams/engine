package com.gurukulams.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurukulams.core.model.Org;
import com.gurukulams.core.service.OrgService;
import com.gurukulams.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
public class OrgLoader {

    /**
     * Logger.
     */
    private final Logger logger =
            LoggerFactory.getLogger(Application.class);

    /**
     * Quetion Owner.
     */
    public static final String USER_NAME = "tom@email.com";
    /**
     * Org Service.
     */
    private final OrgService orgService;

    /**
     * Json Mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * OrgsLoader.
     *
     * @param theOrgService
     * @param theObjectMapper
     */
    public OrgLoader(final OrgService theOrgService,
                     final ObjectMapper theObjectMapper) {
        this.orgService = theOrgService;
        this.objectMapper = theObjectMapper;
    }

    /**
     * Loads Orgs.
     */
    public void load() throws IOException, SQLException {
//        orgService.delete();
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("orgs.json");
        BufferedReader reader
                = new BufferedReader(new InputStreamReader(is));
        List<Org> orgs = objectMapper.readValue(reader,
                new TypeReference<List<Org>>() { });

        for (Org org : orgs) {

            if (orgService.read(USER_NAME, "org-" + org.userHandle(), null)
                    .isEmpty()) {
                orgService.create(USER_NAME, null, org);
            } else {
                org = org.withUserHandle("org-" + org.userHandle());
                orgService.update(org.userHandle(),
                        USER_NAME, null, org);
            }
        }
    }
}
