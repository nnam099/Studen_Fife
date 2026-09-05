import { Body, Controller, Delete, Get, Param, Patch, Post, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { AuthenticatedUser, CurrentUser } from '../common/current-user.decorator';
import { CreateMilestoneDto } from './dto/create-milestone.dto';
import { UpdateMilestoneDto } from './dto/update-milestone.dto';
import { MilestonesService } from './milestones.service';

@Controller('milestones')
@UseGuards(JwtAuthGuard)
export class MilestonesController {
  constructor(private readonly service: MilestonesService) {}
  @Get() findAll(@CurrentUser() user: AuthenticatedUser) { return this.service.findAll(user.sub); }
  @Get(':id') findOne(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string) { return this.service.findOne(user.sub, id); }
  @Post() create(@CurrentUser() user: AuthenticatedUser, @Body() dto: CreateMilestoneDto) { return this.service.create(user.sub, dto); }
  @Patch(':id') update(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string, @Body() dto: UpdateMilestoneDto) { return this.service.update(user.sub, id, dto); }
  @Delete(':id') remove(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string) { return this.service.remove(user.sub, id); }
}
