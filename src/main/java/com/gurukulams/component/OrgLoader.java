package com.gurukulams.component;

import com.gurukulams.core.model.Org;
import com.gurukulams.core.service.OrgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
public class OrgLoader {
    /**
     * Quetion Owner.
     */
    public static final String USER_NAME = "tom@email.com";

    /**
     * Logger.
     */
    private final Logger logger =
            LoggerFactory.getLogger(OrgLoader.class);

    /**
     * Org Service.
     */
    private final OrgService orgService;

    /**
     * Json Mapper.
     */
    private final JsonMapper jsonMapper;

    /**
     * OrgsLoader.
     *
     * @param theOrgService
     * @param theJsonMapper
     */
    public OrgLoader(final OrgService theOrgService,
                     final JsonMapper theJsonMapper) {
        this.orgService = theOrgService;
        this.jsonMapper = theJsonMapper;
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
        List<Org> orgs = jsonMapper.readValue(reader,
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
