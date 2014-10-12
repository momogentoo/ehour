/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.rrm.ehour.project.service;

import net.rrm.ehour.activity.service.ActivityService;
import net.rrm.ehour.domain.Activity;
import net.rrm.ehour.domain.Project;
import net.rrm.ehour.domain.User;
import net.rrm.ehour.exception.ObjectNotFoundException;
import net.rrm.ehour.exception.ParentChildConstraintException;
import net.rrm.ehour.persistence.project.dao.ProjectDao;
import net.rrm.ehour.user.service.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Project service
 */
@Service("projectService")
public class ProjectServiceImpl implements ProjectService {
    private static final Logger LOGGER = Logger.getLogger(ProjectServiceImpl.class);

    private ProjectDao projectDAO;

    private ActivityService activityService;

    private UserService userService;

    @Autowired
    public ProjectServiceImpl(ProjectDao projectDAO, ActivityService activityService, UserService userService) {
        this.projectDAO = projectDAO;
        this.activityService = activityService;
        this.userService = userService;
    }

    @Override
    public List<Project> getProjects() {
        return projectDAO.findAll();
    }

    @Override
    public List<Project> getActiveProjects() {
        return projectDAO.findAllActive();
    }

    public Project getProject(Integer projectId) throws ObjectNotFoundException {
        Project project = projectDAO.findById(projectId);

        if (project == null) {
            throw new ObjectNotFoundException("Project not found for id " + projectId);
        }

        return project;
	}

    @Override
    public Project getProject(String projectCode) {
        return projectDAO.findByProjectCode(projectCode);
    }

    public Project getProjectAndCheckDeletability(Integer projectId) throws ObjectNotFoundException
	{
		Project project = getProject(projectId);
		
		setProjectDeletability(project);
		
		return project;
	}

    public void setProjectDeletability(Project project) {
        // broken impl
        project.setDeletable(true);
    }

    @Transactional
    @Override
    public Project createProject(Project project) {
        return updateProject(project);
    }


    @Transactional
    @Override
    public Project updateProject(Project project) {
        projectDAO.persist(project);

        return project;
    }

    @Transactional
    public void deleteProject(Integer projectId) throws ParentChildConstraintException {
        Project project;

        project = projectDAO.findById(projectId);

        deleteEmptyActivities(project);
        LOGGER.debug("Deleting project " + project);
        projectDAO.delete(project);
    }

    private void deleteEmptyActivities(Project project) throws ParentChildConstraintException {
        checkProjectDeletability(project);

        if (project.getActivities() != null &&
                project.getActivities().size() > 0) {
            deleteAnyActivities(project);
        }
    }

    private void deleteAnyActivities(Project project) throws ParentChildConstraintException {
        for (Activity activity : project.getActivities()) {
            activityService.deleteActivity(activity.getId());
        }

        project.getActivities().clear();
    }

    private void checkProjectDeletability(Project project) throws ParentChildConstraintException {
        setProjectDeletability(project);
    }

    public List<Project> getProjectManagerProjects(User user) {
        return projectDAO.findActiveProjectsWhereUserIsPM(user);
    }
}
