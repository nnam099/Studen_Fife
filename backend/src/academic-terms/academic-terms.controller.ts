import { Body, Controller, Delete, Get, Param, Patch, Post, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { CurrentUser, AuthenticatedUser } from '../common/current-user.decorator';
import { CreateAcademicTermDto } from './dto/create-academic-term.dto';
import { UpdateAcademicTermDto } from './dto/update-academic-term.dto';
import { AcademicTermsService } from './academic-terms.service';

@Controller('academic-terms')
@UseGuards(JwtAuthGuard)
export class AcademicTermsController {
  constructor(private readonly service: AcademicTermsService) {}

  @Get()
  findAll(@CurrentUser() user: AuthenticatedUser) { return this.service.findAll(user.sub); }

  @Get(':id')
  findOne(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string) { return this.service.findOne(user.sub, id); }

  @Post()
  create(@CurrentUser() user: AuthenticatedUser, @Body() dto: CreateAcademicTermDto) { return this.service.create(user.sub, dto); }

  @Patch(':id')
  update(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string, @Body() dto: UpdateAcademicTermDto) { return this.service.update(user.sub, id, dto); }

  @Delete(':id')
  remove(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string) { return this.service.remove(user.sub, id); }
}
